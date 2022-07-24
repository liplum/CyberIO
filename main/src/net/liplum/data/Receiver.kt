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
import mindustry.world.blocks.ItemSelection
import mindustry.world.meta.BlockGroup
import net.liplum.DebugOnly
import net.liplum.R
import net.liplum.UndebugOnly
import net.liplum.Var
import net.liplum.api.cyber.*
import net.liplum.blocks.AniedBlock
import net.liplum.common.Changed
import net.liplum.common.delegates.Delegate1
import net.liplum.common.persistence.read
import net.liplum.common.persistence.write
import net.liplum.data.Receiver.ReceiverBuild
import net.liplum.lib.Serialized
import net.liplum.lib.assets.TR
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.animations.anims.Animation
import net.liplum.mdt.animations.anis.AniState
import net.liplum.mdt.animations.anis.config
import net.liplum.mdt.render.Draw
import net.liplum.mdt.render.DrawOn
import net.liplum.mdt.render.SetColor
import net.liplum.mdt.ui.bars.removeItemsInBar
import net.liplum.mdt.utils.autoAnimInMod
import net.liplum.mdt.utils.inMod
import net.liplum.mdt.utils.sub
import net.liplum.utils.addSenderInfo

private typealias AniStateR = AniState<Receiver, ReceiverBuild>

open class Receiver(name: String) : AniedBlock<Receiver, ReceiverBuild>(name) {
    @ClientOnly lateinit var BaseTR: TR
    @ClientOnly lateinit var HighlightTR: TR
    @ClientOnly lateinit var DownArrowTR: TR
    @ClientOnly lateinit var UnconnectedTR: TR
    @ClientOnly lateinit var NoPowerTR: TR
    @ClientOnly lateinit var DownloadAnim: Animation
    @JvmField var maxConnection = -1
    @JvmField var DownloadAnimFrameNumber = 7
    @JvmField var DownloadAnimDuration = 30f
    @JvmField var blockTime = 60f
    @JvmField var fullTime = 60f
    @JvmField val CheckConnectionTimer = timers++
    @JvmField val TransferTimer = timers++

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
        callDefaultBlockDraw = false
        config(
            Item::class.java
        ) { obj: ReceiverBuild, item ->
            obj.outputItem = item
        }
        configClear { tile: ReceiverBuild -> tile.outputItem = null }
    }

    override fun drawPlace(x: Int, y: Int, rotation: Int, valid: Boolean) {
        super.drawPlace(x, y, rotation, valid)
        this.drawLinkedLineToReceiverWhenConfiguring(x, y)
    }

    override fun load() {
        super.load()
        BaseTR = this.sub("base")
        HighlightTR = this.sub("highlight")
        DownArrowTR = this.inMod("rs-down-arrow")
        UnconnectedTR = this.inMod("rs-unconnected")
        NoPowerTR = this.inMod("rs-no-power")
        DownloadAnim = this.autoAnimInMod("rs-down-arrow", DownloadAnimFrameNumber, DownloadAnimDuration)
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
        addMaxHostStats(1)
    }

    override fun drawPlanConfig(req: BuildPlan, list: Eachable<BuildPlan>) {
        drawPlanConfigCenter(req, req.config, "center", true)
    }

    override fun outputsItems(): Boolean = true
    open inner class ReceiverBuild : AniedBuild(), IDataReceiver {
        @Serialized
        var outputItem: Item? = null
            set(value) {
                if (field != value) {
                    lastReceiverColor = Changed(field?.color)
                    field = value
                    onRequirementUpdated(this)
                }
            }
        override val receiverColor: Color
            get() = outputItem?.color ?: R.C.Receiver
        override var lastReceiverColor: Changed<Color> = Changed.empty()
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
                this.drawDataNetGraphic()
            }
            this.drawRequirements()
        }

        override fun updateTile() {
            // Check connection every second
            if (timer(CheckConnectionTimer, 60f)) {
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
                if (efficiency > 0f && timer(TransferTimer, 1f)) {
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
        override fun fixedDraw() {
            BaseTR.DrawOn(this)
            Draw.alpha(Var.rsSlightHighlightAlpha)
            HighlightTR.DrawOn(this)
            Draw.color()
        }
    }

    @ClientOnly lateinit var DownloadAni: AniStateR
    @ClientOnly lateinit var UnconnectedAni: AniStateR
    @ClientOnly lateinit var BlockedAni: AniStateR
    @ClientOnly lateinit var NoPowerAni: AniStateR
    override fun genAniState() {
        NoPowerAni = addAniState("NoPower") {
            NoPowerTR.Draw(x, y)
        }
        DownloadAni = addAniState("Download") {
            DownloadAnim.draw(Color.green, x, y)
        }
        UnconnectedAni = addAniState("Unconnected") {
            SetColor(R.C.Unconnected)
            UnconnectedTR.Draw(x, y)
            Draw.color()
        }
        BlockedAni = addAniState("Blocked") {
            SetColor(R.C.Stop)
            DownArrowTR.Draw(x, y)
            Draw.color()
        }
    }

    override fun genAniConfig() {
        config {
            // UnconnectedAni
            From(UnconnectedAni) To DownloadAni When {
                outputItem != null
            } To NoPowerAni When {
                !canConsume()
            }
            // BlockedAni
            From(BlockedAni) To UnconnectedAni When {
                outputItem == null
            } To DownloadAni When {
                !isBlocked || lastFullDataDelta < fullTime
            } To NoPowerAni When {
                !canConsume()
            }
            // DownloadAni
            From(DownloadAni) To UnconnectedAni When {
                outputItem == null
            } To BlockedAni When {
                isBlocked && lastFullDataDelta > fullTime
            } To NoPowerAni When {
                !canConsume()
            }
            // NoPower
            From(NoPowerAni) To UnconnectedAni When {
                canConsume() && outputItem == null
            } To DownloadAni When {
                canConsume() && outputItem != null
            }
        }
    }
}