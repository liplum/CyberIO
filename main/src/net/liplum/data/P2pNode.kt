package net.liplum.data

import arc.func.Prov
import arc.graphics.Color
import arc.math.Angles
import arc.util.Eachable
import arc.util.Time
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.Vars
import mindustry.entities.units.BuildPlan
import mindustry.gen.Building
import mindustry.graphics.Layer
import mindustry.logic.LAccess
import mindustry.type.Liquid
import mindustry.world.Block
import mindustry.world.blocks.liquid.LiquidBlock
import mindustry.world.meta.BlockGroup
import net.liplum.DebugOnly
import net.liplum.R
import net.liplum.Var
import net.liplum.Var.YinYangRotationSpeed
import net.liplum.api.cyber.*
import net.liplum.common.Remember
import net.liplum.utils.CalledBySync
import net.liplum.utils.SendDataPack
import net.liplum.render.Text
import net.liplum.utils.sub
import net.liplum.utils.addStateMachineInfo
import net.liplum.utils.update
import plumy.animation.ContextDraw.Draw
import plumy.animation.ContextDraw.DrawOn
import plumy.animation.state.IStateful
import plumy.animation.state.State
import plumy.animation.state.StateConfig
import plumy.animation.state.configuring
import plumy.core.ClientOnly
import plumy.core.Serialized
import plumy.core.WhenNotPaused
import plumy.core.assets.EmptyTR
import plumy.core.math.nextBoolean
import plumy.dsl.*
import kotlin.math.absoluteValue

open class P2pNode(name: String) : Block(name), IDataBlock {
    @ClientOnly var liquidPadding = 0f
    @ClientOnly var NoPowerTR = EmptyTR
    @JvmField var balancingSpeed = 1f
    @JvmField var dumpScale = 2f
    /**
     * The max range when trying to connect. -1f means no limit.
     */
    @JvmField var maxRange = -1f
    @ClientOnly @JvmField var maxSelectedCircleTime = Var.SelectedCircleTime
    @ClientOnly var BottomTR = EmptyTR
    @ClientOnly var YinAndYangTR = EmptyTR
    @ClientOnly var stateMachineConfig = StateConfig<P2pBuild>()

    init {
        buildType = Prov { P2pBuild() }
        hasLiquids = true
        update = true
        solid = true
        group = BlockGroup.liquids
        outputsLiquid = true
        configurable = true
        noUpdateDisabled = true
        canOverdrive = false
        sync = true
    }

    override fun init() {
        super.init()
        config<P2pBuild, PackedPos> {
            connectedPos = it
        }
        configNull<P2pBuild> {
            connected?.connectedPos = -1
            connectedPos = -1
        }
        ClientOnly {
            configAnimationStateMachine()
        }
    }

    override fun load() {
        super.load()
        BottomTR = this.sub("bottom")
        NoPowerTR = loadNoPower()
        YinAndYangTR = this.sub("yin-and-yang")
    }

    override fun drawPlace(x: Int, y: Int, rotation: Int, valid: Boolean) {
        super.drawPlace(x, y, rotation, valid)
        drawPlacingMaxRange(x, y, maxRange, R.C.P2P)
        drawLinkedLineToP2pWhenConfiguring(x, y)
    }

    override fun drawPlanRegion(plan: BuildPlan, list: Eachable<BuildPlan>) {
        super.drawPlanRegion(plan, list)
        drawPlanMaxRange(plan.x, plan.y, maxRange, R.C.P2P)
    }

    override fun setStats() {
        super.setStats()
        addLinkRangeStats(maxRange)
        addDataTransferSpeedStats(balancingSpeed)
    }

    override fun setBars() {
        super.setBars()
        addP2pLinkInfo<P2pBuild>()
        DebugOnly {
            addStateMachineInfo<P2pBuild>()
        }
    }

    override fun icons() = arrayOf(BottomTR, region, YinAndYangTR)
    open inner class P2pBuild : Building(),
        IStateful<P2pBuild>, IP2pNode {
        override val stateMachine by lazy { stateMachineConfig.instantiate(this) }
        override val maxRange = this@P2pNode.maxRange
        override val currentFluid: Liquid
            get() = liquids.current()
        override val currentAmount: Float
            get() = liquids.currentAmount()
        @Serialized
        @set:CalledBySync
        override var connectedPos: PackedPos = -1
        @ClientOnly
        override var isDrawer: Boolean = false
        @ClientOnly
        override var status = P2pStatus.None
        @SendDataPack(["connectedPos::set"])
        override fun connectToSync(other: IP2pNode) {
            configure(other.building.pos())
        }

        override fun disconnectFromAnotherSync() {
            configure(null)
        }
        /**
         * ### If unconnected
         * limit:
         * 1. Does this match the input?
         * 2. If 1 is false, is this almost empty?
         * 3. Can this hold more fluid?
         * ### If connected
         * limit:
         * 1. Does another match the input?
         * 2. If 1 is false, is another almost empty?
         * 3. Does this match the input?
         * 3. If 3 is false, is this almost empty?
         * 4. Can this hold more fluid?
         */
        override fun acceptLiquid(source: Building, fluid: Liquid): Boolean {
            val connected = connected
            return if (connected == null)
                (currentFluid == fluid || currentAmount < 0.2f) && liquids[fluid] < liquidCapacity
            else
                (connected.currentFluid == fluid || connected.currentAmount < 0.2f) &&
                        (this.currentFluid == fluid || this.currentAmount < 0.2f) &&
                        this.liquids[fluid] < liquidCapacity
        }

        fun balanceFluid() {
            val other = connected
            run {
                if (other != null) {
                    if (this.currentFluid != other.currentFluid && other.currentAmount > 0.02f) return@run
                    val difference = this.currentAmount - other.currentAmount
                    val connection =
                        when {
                            difference > 0f -> this to other //this > other, other needs more
                            difference < 0f -> other to this //this < other, this needs more
                            else -> return@run
                        }
                    val (sender, receiver) = connection
                    val abs = difference.absoluteValue
                    if (abs > Var.P2pNNodeBalanceThreshold) {
                        sender.status = P2pStatus.Sender
                        receiver.status = P2pStatus.Receiver
                        if (other.building.efficiency > 0f) {
                            val data = abs
                                .coerceAtMost(sender.currentAmount)
                                .coerceAtMost(receiver.restRoom)
                                .coerceAtMost(balancingSpeed * efficiency * other.building.efficiency * delta())
                            if (data > 0.0001f) {
                                sender.streamToAnother(data)
                            }
                        }
                    }
                }
            }
        }
        @ClientOnly
        var lastP2pColor = Remember.empty<Color>()
        @ClientOnly
        var targetP2pColor = R.C.P2P
        override val color: Color
            get() = transitionColor(lastP2pColor, targetP2pColor)
        var lastTileChange = -2
        override fun updateTile() {
            // Check connection only when any block changed
            if (lastTileChange != Vars.world.tileChanges) {
                lastTileChange = Vars.world.tileChanges
                checkConnection()
            }
            balanceFluid()
            if (currentAmount > 0.0001f && timer(timerDump, 1f)) {
                dumpLiquid(currentFluid, dumpScale)
            }
            ClientOnly {
                val other = connected ?: return@ClientOnly
                if (other.isDrawer == this.isDrawer) {
                    this.isDrawer = nextBoolean()
                    other.isDrawer = !this.isDrawer
                }
            }
            ClientOnly {
                val other = connected
                if (other == null) {
                    if (targetP2pColor != R.C.P2P) {
                        lastP2pColor = Remember(old = targetP2pColor)
                        targetP2pColor = R.C.P2P
                    }
                } else {
                    if (currentAmount < 0.0001f) {
                        if (targetP2pColor != R.C.P2P) {
                            lastP2pColor = Remember(old = targetP2pColor)
                            targetP2pColor = R.C.P2P
                        }
                    } else {
                        val fluidColor = currentFluid.fluidColor
                        if (targetP2pColor != fluidColor) {
                            lastP2pColor = Remember(old = targetP2pColor)
                            targetP2pColor = fluidColor
                        }
                    }
                }
            }
        }
        @ClientOnly
        var yinYangRotation = 0f
        override fun draw() {
            stateMachine.update(delta())
            BottomTR.DrawOn(this)
            if (currentAmount > 0.001f) {
                LiquidBlock.drawTiledFrames(
                    size, x, y, liquidPadding,
                    currentFluid, currentAmount / liquidCapacity
                )
            }
            region.DrawOn(this)
            WhenNotPaused {
                yinYangRotation = when (status) {
                    P2pStatus.Sender -> Angles.moveToward(yinYangRotation, 0f, YinYangRotationSpeed * Time.delta)
                    P2pStatus.Receiver -> Angles.moveToward(yinYangRotation, 180f, YinYangRotationSpeed * Time.delta)
                    else -> Angles.moveToward(yinYangRotation, 0f, YinYangRotationSpeed * Time.delta)
                }
            }
            YinAndYangTR.DrawOn(this, rotation = yinYangRotation)
            val other = connected
            DebugOnly {
                if (other != null) {
                    DrawLayer(Layer.blockOver) {
                        Text.drawTextEasy("(${other.tileX},${other.tileY})", x, y + 10f)
                    }
                }
            }
            stateMachine.draw()
        }

        override fun streamToAnother(amount: Float) {
            connected?.let {
                liquids.remove(currentFluid, amount)
                it.readSteam(currentFluid, amount)
            }
        }

        override fun readSteam(fluid: Liquid, amount: Float) {
            liquids.add(fluid, amount)
        }

        override fun drawSelect() {
            super.drawSelect()
            drawP2PConnection()
            drawSelectedMaxRange()
        }

        override fun drawConfigure() {
            super.drawConfigure()
            drawConfiguringMaxRange()
        }
        @ClientOnly
        @SendDataPack
        override fun onConfigureBuildTapped(other: Building): Boolean {
            if (this == other) {
                deselect()
                configure(null)
                return false
            }
            if (other == connected) {
                configure(null)
                return false
            }
            if (other is IP2pNode) {
                if (maxRange > 0f && other.dst(this) >= maxRange) {
                    postOverRangeOn(other)
                } else {
                    connected?.disconnectFromAnotherSync()
                    other.connected?.disconnectFromAnotherSync()
                    this.connectToSync(other)
                    other.connectToSync(this)
                    this.isDrawer = true
                    other.isDrawer = false
                }
                return false
            }
            return true
        }

        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            connectedPos = read.i()
        }

        override fun write(write: Writes) {
            super.write(write)
            write.i(connectedPos)
        }

        override fun control(type: LAccess, p1: Any?, p2: Double, p3: Double, p4: Double) {
            when (type) {
                LAccess.shoot ->
                    if (p1 is IP2pNode) connectToSync(p1)
                else -> super.control(type, p1, p2, p3, p4)
            }
        }

        override fun control(type: LAccess, p1: Double, p2: Double, p3: Double, p4: Double) {
            when (type) {
                LAccess.shoot -> {
                    val receiver = Vars.world.build(p1, p2)
                    if (receiver is IP2pNode) connectToSync(receiver)
                }
                else -> super.control(type, p1, p2, p3, p4)
            }
        }

        override fun sense(sensor: LAccess): Double {
            return when (sensor) {
                LAccess.shootX -> connected.tileXd
                LAccess.shootY -> connected.tileYd
                else -> super.sense(sensor)
            }
        }
    }

    @ClientOnly lateinit var NormalState: State<P2pBuild>
    @ClientOnly lateinit var NoPowerState: State<P2pBuild>
    fun configAnimationStateMachine() {
        NormalState = State("Normal")
        NoPowerState = State("NoPower") {
            NoPowerTR.Draw(x, y)
        }
        stateMachineConfig.configuring {
            NormalState {
                setDefaultState
                NoPowerState { !canConsume() }
            }
            NoPowerState {
                NormalState { canConsume() }
            }
        }
    }
}