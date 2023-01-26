package net.liplum.data

import arc.func.Prov
import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.scene.ui.layout.Table
import arc.struct.OrderedSet
import arc.util.Eachable
import arc.util.Time
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.Vars
import mindustry.entities.units.BuildPlan
import mindustry.gen.Building
import mindustry.type.Item
import mindustry.world.Block
import mindustry.world.blocks.ItemSelection
import mindustry.world.meta.BlockGroup
import net.liplum.DebugOnly
import net.liplum.R
import net.liplum.UndebugOnly
import net.liplum.Var
import net.liplum.api.cyber.*
import net.liplum.common.delegate.Delegate1
import net.liplum.common.persistence.read
import net.liplum.common.persistence.write
import net.liplum.ui.bars.removeItemsInBar
import net.liplum.utils.sub
import net.liplum.utils.update
import plumy.animation.ContextDraw.Draw
import plumy.animation.ContextDraw.DrawOn
import plumy.animation.ContextDraw.SetColor
import plumy.animation.SharedAnimation
import plumy.animation.draw
import plumy.animation.state.IStateful
import plumy.animation.state.State
import plumy.animation.state.StateConfig
import plumy.animation.state.configuring
import plumy.core.ClientOnly
import plumy.core.Serialized
import plumy.core.assets.EmptyTR
import plumy.dsl.config
import plumy.dsl.configNull

open class Receiver(name: String) : Block(name), IDataBlock {
    @ClientOnly var BaseTR = EmptyTR
    @ClientOnly var HighlightTR = EmptyTR
    @ClientOnly var DownArrowTR = EmptyTR
    @ClientOnly var UnconnectedTR = EmptyTR
    @ClientOnly var NoPowerTR = EmptyTR
    @ClientOnly var DownloadAnim = SharedAnimation.Empty
    @JvmField var maxConnection = -1
    @JvmField var blockTime = 60f
    @JvmField var fullTime = 60f
    @ClientOnly var stateMachineConfig = StateConfig<ReceiverBuild>()

    init {
        buildType = Prov { ReceiverBuild() }
        hasItems = true
        update = true
        solid = true
        itemCapacity = 20
        group = BlockGroup.transportation
        configurable = true
        saveConfig = true
        noUpdateDisabled = true
        acceptsItems = false
        schematicPriority = 25
        canOverdrive = false
        allowConfigInventory = false
        sync = true
    }

    override fun init() {
        super.init()
        config<ReceiverBuild, Item> {
            outputItem = it
        }
        configNull<ReceiverBuild> {
            outputItem = null
        }
        ClientOnly {
            configAnimationStateMachine()
        }
    }

    override fun drawPlace(x: Int, y: Int, rotation: Int, valid: Boolean) {
        super.drawPlace(x, y, rotation, valid)
        this.drawLinkedLineToReceiverWhenConfiguring(x, y)
    }

    override fun load() {
        super.load()
        BaseTR = this.sub("base")
        HighlightTR = this.sub("highlight")
        DownArrowTR = loadDownArrow()
        UnconnectedTR = loadUnconnected()
        NoPowerTR = loadNoPower()
        DownloadAnim = loadDownloadAnimation()
    }

    override fun setBars() {
        super.setBars()
        UndebugOnly {
            removeItemsInBar()
        }
        DebugOnly {
            addSenderInfo<ReceiverBuild>()
        }
    }

    override fun setStats() {
        super.setStats()
        addMaxSenderStats(maxConnection)
    }

    override fun drawPlanConfig(req: BuildPlan, list: Eachable<BuildPlan>) {
        drawPlanConfigCenter(req, req.config, "center", true)
    }

    override fun outputsItems(): Boolean = true
    open inner class ReceiverBuild : Building(), IStateful<ReceiverBuild>, IDataReceiver {
        override val stateMachine by lazy { stateMachineConfig.instantiate(this) }
        @Serialized
        var outputItem: Item? = null
            set(value) {
                if (field != value) {
                    field = value
                    onRequirementUpdated(this)
                }
            }
        override val receiverColor: Color
            get() = outputItem?.color ?: R.C.Receiver
        @ClientOnly
        var lastOutputDelta = 0f
        @ClientOnly
        var lastFullDataDelta = 0f
        @Serialized
        var senders = OrderedSet<Int>()
        override val onRequirementUpdated: Delegate1<IDataReceiver> = Delegate1()
        override fun onRemoved() {
            onRequirementUpdated.clear()
        }
        @ClientOnly
        val isBlocked: Boolean
            get() = lastOutputDelta > blockTime

        override fun drawSelect() {
            whenNotConfiguringSender {
                this.drawDataNetGraph()
            }
            this.drawRequirements()
        }

        var lastTileChange = -2
        override fun updateTile() {
            // Check connection only when any block changed
            if (lastTileChange != Vars.world.tileChanges) {
                lastTileChange = Vars.world.tileChanges
                checkSendersPos()
            }
            ClientOnly {
                lastOutputDelta += Time.delta
            }
            val outputItem = outputItem
            if (outputItem != null) {
                ClientOnly {
                    val isFullData = items[outputItem] < getMaximumAccepted(outputItem)
                    if (isFullData) {
                        lastFullDataDelta = 0f
                    } else {
                        lastFullDataDelta += Time.delta
                    }
                }
                if (efficiency > 0f && timer(timerDump, 1f)) {
                    val dumped = dump(outputItem)
                    ClientOnly {
                        if (dumped) {
                            lastOutputDelta = 0f
                        }
                    }
                }
            }
        }

        override fun buildConfiguration(table: Table) {
            ItemSelection.buildTable(table, Vars.content.items(),
                { outputItem },
                { value: Item? -> configure(value) })
        }

        override fun onConfigureBuildTapped(other: Building): Boolean {
            if (this == other) {
                deselect()
                configure(null)
                return false
            }
            return true
        }

        override fun acceptItem(source: Building, item: Item): Boolean = false
        override fun getAcceptedAmount(sender: IDataSender, item: Item): Int {
            if (!canConsume()) return 0

            return if (item == outputItem)
                getMaximumAccepted(outputItem) - items[outputItem]
            else 0
        }

        override fun receiveDataFrom(sender: IDataSender, item: Item, amount: Int) {
            if (this.isConnectedTo(sender)) {
                items.add(item, amount)
            }
        }

        override val requirements
            get() = outputItem.req

        override fun config(): Item? = outputItem
        override fun write(write: Writes) {
            super.write(write)
            val outputItem = outputItem
            write.s(outputItem?.id?.toInt() ?: -1)
            senders.write(write)
        }

        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            outputItem = Vars.content.item(read.s().toInt())
            senders.read(read)
        }

        override val connectedSenders = senders
        override val maxSenderConnection = maxConnection
        override fun draw() {
            stateMachine.update(delta())
            BaseTR.DrawOn(this)
            Draw.alpha(Var.RsSlightHighlightAlpha)
            HighlightTR.DrawOn(this)
            Draw.color()
            stateMachine.draw()
        }
    }

    @ClientOnly lateinit var DownloadState: State<ReceiverBuild>
    @ClientOnly lateinit var UnconnectedState: State<ReceiverBuild>
    @ClientOnly lateinit var BlockedState: State<ReceiverBuild>
    @ClientOnly lateinit var NoPowerState: State<ReceiverBuild>
    @ClientOnly
    fun configAnimationStateMachine() {
        NoPowerState = State("NoPower") {
            NoPowerTR.Draw(x, y)
        }
        DownloadState = State("Download") {
            DownloadAnim.draw(Color.green, x, y)
        }
        UnconnectedState = State("Unconnected") {
            SetColor(R.C.Unconnected)
            UnconnectedTR.Draw(x, y)
            Draw.color()
        }
        BlockedState = State("Blocked") {
            SetColor(R.C.Stop)
            DownArrowTR.Draw(x, y)
            Draw.color()
        }
        stateMachineConfig.configuring {
            UnconnectedState {
                DownloadState { outputItem != null }
                NoPowerState { !canConsume() }
            }
            BlockedState {
                UnconnectedState { outputItem == null }
                DownloadState { !isBlocked || lastFullDataDelta < fullTime }
                NoPowerState { !canConsume() }
            }
            DownloadState {
                UnconnectedState { outputItem == null }
                BlockedState { isBlocked && lastFullDataDelta > fullTime }
                NoPowerState { !canConsume() }
            }
            NoPowerState {
                setDefaultState
                UnconnectedState { canConsume() && outputItem == null }
                DownloadState { canConsume() && outputItem != null }
            }
        }
    }
}