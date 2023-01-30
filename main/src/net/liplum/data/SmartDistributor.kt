package net.liplum.data

import arc.func.Boolf
import arc.func.Prov
import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.math.Mathf
import arc.struct.OrderedSet
import arc.struct.Seq
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.Vars
import mindustry.gen.Building
import mindustry.graphics.Pal
import mindustry.type.Item
import mindustry.type.ItemStack
import mindustry.world.Block
import mindustry.world.consumers.Consume
import mindustry.world.consumers.ConsumeItemDynamic
import mindustry.world.consumers.ConsumeItemFilter
import mindustry.world.consumers.ConsumeItems
import mindustry.world.meta.BlockGroup
import mindustry.world.meta.Stat
import net.liplum.DebugOnly
import net.liplum.R
import net.liplum.UndebugOnly
import net.liplum.Var
import net.liplum.api.cyber.*
import net.liplum.common.delegate.Delegate1
import net.liplum.common.persistence.read
import net.liplum.common.persistence.write
import net.liplum.common.util.DoMultipleBool
import plumy.core.ClientOnly
import plumy.animation.AnimationMeta
import plumy.animation.ContextDraw.Draw
import plumy.animation.draw
import plumy.animation.state.IStateful
import plumy.animation.state.State
import plumy.animation.state.StateConfig
import plumy.animation.state.configuring
import net.liplum.render.drawSurroundingRect
import net.liplum.input.smoothPlacing
import net.liplum.input.smoothSelect
import net.liplum.ui.bars.removeItemsInBar
import net.liplum.utils.*
import plumy.core.Serialized
import plumy.core.WhenNotPaused
import plumy.core.arc.equalsNoOrder
import plumy.core.arc.set
import plumy.core.assets.EmptyTR
import plumy.core.math.isZero
import plumy.dsl.AddBar
import kotlin.math.log2

open class SmartDistributor(name: String) : Block(name), IDataBlock {
    @JvmField
    var maxConnection = -1
    @ClientOnly
    var NoPowerTR = EmptyTR
    @ClientOnly
    var ArrowsAnim = AnimationMeta.Empty
    @JvmField
    @ClientOnly
    var ArrowsFrames = 9
    @JvmField
    @ClientOnly
    var ArrowsDuration = 70f
    @JvmField
    @ClientOnly
    var DistributionTime = 60f
    @ClientOnly
    @JvmField
    var maxSelectedCircleTime = Var.SurroundingRectTime
    /**
     * The area(tile xy) indicates the surrounding machines can be distributed.
     */
    @JvmField
    @ClientOnly
    var indicateAreaExtension = 2f
    @JvmField
    var powerUsePerItem = 2.5f
    @JvmField
    var powerUseBase = 3f
    @JvmField
    val TransferTimer = timers++
    @JvmField
    val DynamicReqUpdateTimer = timers++
    @JvmField
    var supportedConsumerFilter = Boolf<Consume> {
        it is ConsumeItems || it is ConsumeItemDynamic || it is ConsumeItemFilter
    }
    @JvmField
    var boost2Count: (Float) -> Int = {
        if (it <= 1.1f)
            1
        else if (it in 1.1f..2.1f)
            2
        else if (it in 2.1f..3f)
            3
        else
            Mathf.round(log2(it + 5.1f))
    }
    @ClientOnly
    var stateMachineConfig = StateConfig<SmartDistributorBuild>()

    init {
        buildType = Prov { SmartDistributorBuild() }
        solid = true
        update = true
        hasItems = true
        itemCapacity = 50
        hasPower = true
        schematicPriority = 25
        group = BlockGroup.transportation
        noUpdateDisabled = true
        canOverdrive = true
        unloadable = false
        allowConfigInventory = false
        sync = true
    }

    open fun initPowerUse() {
        consumePowerDynamic<SmartDistributorBuild> {
            it._requirements.size * powerUsePerItem + powerUseBase
        }
    }

    override fun init() {
        initPowerUse()
        super.init()
        ClientOnly {
            configAnimationStateMachine()
        }
    }

    override fun load() {
        super.load()
        NoPowerTR = loadNoPower()
        ArrowsAnim = this.animationMeta("arrows", ArrowsFrames, ArrowsDuration)
    }

    override fun setStats() {
        super.setStats()
        stats.remove(Stat.powerUse)
        addPowerUseStats()
        addMaxSenderStats(maxConnection)
    }

    override fun setBars() {
        super.setBars()
        UndebugOnly {
            removeItemsInBar()
        }
        DebugOnly {
            addStateMachineInfo<SmartDistributorBuild>()
            AddBar<SmartDistributorBuild>("dis-count",
                { "Count:" + boost2Count(timeScale()) },
                { Pal.power },
                { boost2Count(timeScale()) / 4f }
            )
            addSenderInfo<SmartDistributorBuild>()
        }
    }

    override fun drawPlace(x: Int, y: Int, rotation: Int, valid: Boolean) {
        super.drawPlace(x, y, rotation, valid)
        this.drawLinkedLineToReceiverWhenConfiguring(x, y)
        drawSurroundingRect(
            x, y, indicateAreaExtension * smoothPlacing(maxSelectedCircleTime),
            if (valid) R.C.GreenSafe else R.C.RedAlert,
        ) { b ->
            b.block.consumers.any {
                it is ConsumeItems || it is ConsumeItemDynamic || it is ConsumeItemFilter
            } && !b.isDiagonalTo(this, x, y)
        }
        drawPlaceText(subBundle("tip"), x, y, valid)
    }

    open inner class SmartDistributorBuild : Building(),
        IStateful<SmartDistributorBuild>, IDataReceiver {
        override val stateMachine by lazy { stateMachineConfig.instantiate(this) }
        @JvmField
        var _requirements = Seq<Item>()
        @Serialized
        var senders = OrderedSet<Int>()
        override val onRequirementUpdated: Delegate1<IDataReceiver> = Delegate1()
        @ClientOnly
        var lastDistributionTime = 0f
            set(value) {
                field = value.coerceAtLeast(0f)
            }
        @ClientOnly
        val arrowsAnimObj = ArrowsAnim.instantiate()
        @ClientOnly
        open val isDistributing: Boolean
            get() = lastDistributionTime < DistributionTime
        var hasDynamicRequirements: Boolean = false
        @Serialized
        var disIndex = 0
        @ClientOnly
        override var receiverColor = R.C.Receiver
            set(value) {
                if (field != value) {
                    field = value
                }
            }

        override fun onRemoved() {
            onRequirementUpdated.clear()
        }

        val temp = HashSet<Item>()
        open fun updateRequirements() {
            temp.clear()
            hasDynamicRequirements = false
            for (build in proximity) {
                when (val reqs = build.block.findConsumer<Consume>(supportedConsumerFilter)) {
                    is ConsumeItems -> {
                        for (req in reqs.items) {
                            temp.add(req.item)
                        }
                    }

                    is ConsumeItemDynamic -> {
                        for (req in reqs.items.get(build)) {
                            temp.add(req.item)
                        }
                        hasDynamicRequirements = true
                    }

                    is ConsumeItemFilter -> {
                        for (item in Vars.content.items()) {
                            if (reqs.filter.get(item)) {
                                temp.add(item)
                            }
                        }
                    }
                }
            }
            if (!temp.equalsNoOrder(_requirements)) {
                _requirements.set(temp)
                DebugOnly {
                    requirementsText = genRequirementsText()
                }
                ClientOnly {
                    receiverColor = when (_requirements.size) {
                        0 -> R.C.Receiver
                        1 -> _requirements[0].color
                        else -> {
                            val c = Color.gray.cpy()
                            for (req in _requirements) {
                                c.lerp(req.color, 1f / _requirements.size)
                            }
                            c
                        }
                    }
                }
                onRequirementUpdated(this)
            }
        }

        override fun onProximityUpdate() {
            super.onProximityUpdate()
            updateRequirements()
        }

        override fun onProximityAdded() {
            super.onProximityAdded()
            updateRequirements()
        }

        var lastTileChange = -2
        override fun updateTile() {
            // Check connection only when any block changed
            if (lastTileChange != Vars.world.tileChanges) {
                lastTileChange = Vars.world.tileChanges
                checkSendersPos()
            }
            if (hasDynamicRequirements) {
                if (timer(DynamicReqUpdateTimer, 1f)) {
                    updateRequirements()
                }
            }
            if (efficiency > 0f && timer(TransferTimer, 1f)) {
                val dised = DoMultipleBool(canOverdrive, boost2Count(timeScale), this::distribute)
                if (dised) {
                    lastDistributionTime = 0f
                }
            }
            lastDistributionTime += delta()
        }

        open fun distribute(): Boolean {
            if (!block.hasItems || items.total() == 0) {
                return false
            }
            var dised = false
            if (proximity.isEmpty) return false
            disIndex %= proximity.size
            val b = proximity[disIndex]
            when (val reqs = b.block.findConsumer<Consume>(supportedConsumerFilter)) {
                is ConsumeItems -> {
                    dised = distributeTo(b, reqs.items)
                }

                is ConsumeItemDynamic -> {
                    dised = distributeTo(b, reqs.items.get(b))
                }

                is ConsumeItemFilter -> {
                    items.each { item, _ ->
                        dised = dised or distributeTo(b, item)
                    }
                }
            }
            disIndex++
            return dised
        }

        protected open fun distributeTo(other: Building, reqs: Array<ItemStack>): Boolean {
            for (req in reqs) {
                val item = req.item
                if (items.has(item) && other.acceptItem(this, item)) {
                    other.handleItem(this, item)
                    items.remove(item, 1)
                    return true
                }
            }
            return false
        }

        protected open fun distributeTo(other: Building, req: ItemStack): Boolean {
            val item = req.item
            if (items.has(item) && other.acceptItem(this, item)) {
                other.handleItem(this, item)
                items.remove(item, 1)
                return true
            }
            return false
        }

        protected open fun distributeTo(other: Building, reqItem: Item): Boolean {
            if (items.has(reqItem) && other.acceptItem(this, reqItem)) {
                other.handleItem(this, reqItem)
                items.remove(reqItem, 1)
                return true
            }
            return false
        }

        override fun receiveDataFrom(sender: IDataSender, item: Item, amount: Int) {
            if (this.isConnectedTo(sender)) {
                items.add(item, amount)
            }
        }

        override fun getAcceptedAmount(sender: IDataSender, item: Item): Int {
            if (!canConsume()) return 0

            return if (item in _requirements)
                getMaximumAccepted(item) - items[item]
            else
                0
        }

        override val requirements: Seq<Item>?
            get() = _requirements
        @ClientOnly
        val isBlocked: Boolean
            get() = lastDistributionTime > 30f

        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            senders.read(read)
            disIndex = read.b().toInt()
        }

        override fun write(write: Writes) {
            super.write(write)
            senders.write(write)
            write.b(disIndex)
        }
        @DebugOnly
        var requirementsText: String = ""
        @DebugOnly
        fun genRequirementsText() = _requirements.genText()
        override fun drawSelect() {
            whenNotConfiguringSender {
                this.drawDataNetGraph()
            }
            this.drawRequirements()
            DebugOnly {
                if (requirementsText.isNotEmpty()) {
                    drawPlaceText(requirementsText, tileX(), tileY(), true)
                }
            }
            drawSurroundingRect(
                tileX, tileY, indicateAreaExtension * smoothSelect(maxSelectedCircleTime),
                R.C.GreenSafe,
            ) { b ->
                b.block.consumers.any {
                    it is ConsumeItems || it is ConsumeItemDynamic || it is ConsumeItemFilter
                } && !b.isDiagonalTo(this.block, tileX, tileY)
            }
        }

        override fun draw() {
            stateMachine.update(delta())
            WhenNotPaused {
                if (canConsume() && isDistributing)
                    arrowsAnimObj.spend(delta())
            }
            super.draw()
            stateMachine.draw()
        }

        override val connectedSenders = senders
        override val maxSenderConnection = maxConnection
    }

    @ClientOnly
    lateinit var DistributingState: State<SmartDistributorBuild>
    @ClientOnly
    lateinit var NoPowerState: State<SmartDistributorBuild>
    @ClientOnly
    lateinit var NoDistributeState: State<SmartDistributorBuild>
    fun configAnimationStateMachine() {
        DistributingState = State("Distributing") {
            Draw.color(team.color)
            arrowsAnimObj.draw(x, y)
        }
        NoDistributeState = State("NoDistribute") {
            arrowsAnimObj.draw(R.C.Stop, x, y)
        }
        NoPowerState = State("NoPower") {
            NoPowerTR.Draw(x, y)
        }
        stateMachineConfig.configuring {
            NoPowerState {
                setDefaultState
                DistributingState { !power.status.isZero && isDistributing }
                NoDistributeState { !power.status.isZero && !isDistributing }
            }
            NoDistributeState {
                NoPowerState { power.status.isZero }
                DistributingState { isDistributing }
            }

            DistributingState {
                NoPowerState { power.status.isZero }
                NoDistributeState { !isDistributing }
            }
        }
    }
}