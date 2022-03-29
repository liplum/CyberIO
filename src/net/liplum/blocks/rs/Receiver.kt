package net.liplum.blocks.rs

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
import mindustry.world.Tile
import mindustry.world.blocks.ItemSelection
import mindustry.world.meta.BlockGroup
import net.liplum.*
import net.liplum.animations.anims.Animation
import net.liplum.animations.anis.AniState
import net.liplum.animations.anis.Draw
import net.liplum.animations.anis.SetColor
import net.liplum.animations.anis.config
import net.liplum.api.cyber.*
import net.liplum.blocks.AniedBlock
import net.liplum.blocks.rs.Receiver.ReceiverBuild
import net.liplum.lib.delegates.Delegate1
import net.liplum.lib.ui.bars.removeItems
import net.liplum.persistance.intSet
import net.liplum.utils.TR
import net.liplum.utils.addSenderInfo
import net.liplum.utils.autoAnimInMod
import net.liplum.utils.inMod

private typealias AniStateR = AniState<Receiver, ReceiverBuild>

open class Receiver(name: String) : AniedBlock<Receiver, ReceiverBuild>(name) {
    @ClientOnly lateinit var DownArrowTR: TR
    @ClientOnly lateinit var UnconnectedTR: TR
    @ClientOnly lateinit var NoPowerTR: TR
    @ClientOnly lateinit var DownloadAnim: Animation
    @JvmField var maxConnection = -1
    @JvmField var DownloadAnimFrameNumber = 7
    @JvmField var DownloadAnimDuration = 30f
    @JvmField var blockTime = 60f
    @JvmField var fullTime = 60f

    init {
        hasItems = true
        update = true
        solid = true
        itemCapacity = 20
        group = BlockGroup.transportation
        configurable = true
        saveConfig = true
        noUpdateDisabled = true
        acceptsItems = false
        canOverdrive = false
        allowConfigInventory = false
        sync = true

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
        DownArrowTR = this.inMod("rs-down-arrow")
        UnconnectedTR = this.inMod("rs-unconnected")
        NoPowerTR = this.inMod("rs-no-power")
        DownloadAnim = this.autoAnimInMod("rs-down-arrow", DownloadAnimFrameNumber, DownloadAnimDuration)
    }

    override fun setBars() {
        super.setBars()
        UndebugOnly {
            bars.removeItems()
        }
        DebugOnly {
            bars.addSenderInfo<ReceiverBuild>()
        }
    }

    override fun drawRequestConfig(req: BuildPlan, list: Eachable<BuildPlan>) {
        drawRequestConfigCenter(req, req.config, "center", true)
    }

    override fun outputsItems(): Boolean = true
    open inner class ReceiverBuild : AniedBuild(), IDataReceiver {
        var outputItem: Item? = null
            set(value) {
                if (field != value) {
                    field = value
                    onRequirementUpdated(this)
                }
            }
        @ClientOnly
        var lastOutputDelta = 0f
        @ClientOnly
        var lastFullDataDelta = 0f
        var senders = OrderedSet<Int>()
        open fun checkSenderPos() {
            senders.removeAll { !it.ds().exists }
        }

        @JvmField var onRequirementUpdated: Delegate1<IDataReceiver> = Delegate1()
        override fun getOnRequirementUpdated() = onRequirementUpdated
        override fun onRemoved() {
            onRequirementUpdated.clear()
        }
        @ClientOnly
        override fun isBlocked(): Boolean = lastOutputDelta > blockTime
        override fun drawSelect() {
            whenNotConfiguringSender {
                this.drawDataNetGraphic()
            }
            this.drawRequirements()
        }

        override fun updateTile() {
            // Check connection every second
            if (Time.time % 60f < 1) {
                checkSenderPos()
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
                if (consValid()) {
                    val dumped = dump(outputItem)
                    ClientOnly {
                        if (dumped) {
                            lastOutputDelta = 0f
                        }
                    }
                }
                ClientOnly {
                    lastOutputDelta += Time.delta
                }
            }
        }

        override fun buildConfiguration(table: Table) {
            ItemSelection.buildTable(table, Vars.content.items(),
                { outputItem },
                { value: Item? -> configure(value) })
        }

        override fun onConfigureTileTapped(other: Building): Boolean {
            if (this === other) {
                deselect()
                configure(null)
                return false
            }
            return true
        }

        override fun acceptItem(source: Building, item: Item): Boolean = false
        override fun acceptedAmount(sender: IDataSender, itme: Item): Int {
            if (!consValid()) return 0

            return if (itme == outputItem)
                getMaximumAccepted(outputItem) - items[outputItem]
            else
                0
        }

        override fun receiveData(sender: IDataSender, item: Item, amount: Int) {
            if (this.isConnectedWith(sender)) {
                items.add(item, amount)
            }
        }

        override fun getRequirements() = outputItem.req
        override fun config(): Item? = outputItem
        override fun write(write: Writes) {
            super.write(write)
            val outputItem = outputItem
            write.s(outputItem?.id?.toInt() ?: -1)
            write.intSet(senders)
        }

        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            outputItem = Vars.content.item(read.s().toInt())
            senders = read.intSet()
        }
        @CalledBySync
        override fun connect(sender: IDataSender) {
            senders.add(sender.building.pos())
        }
        @CalledBySync
        override fun disconnect(sender: IDataSender) {
            senders.remove(sender.building.pos())
        }

        override fun connectedSenders() = senders
        override fun maxSenderConnection() = maxConnection
        override fun getBuilding(): Building = this
        override fun getTile(): Tile = tile()
        override fun getBlock(): Block = block()
    }

    @ClientOnly lateinit var DownloadAni: AniStateR
    @ClientOnly lateinit var UnconnectedAni: AniStateR
    @ClientOnly lateinit var BlockedAni: AniStateR
    @ClientOnly lateinit var NoPowerAni: AniStateR
    override fun genAniState() {
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
        NoPowerAni = addAniState("NoPower") {
            NoPowerTR.Draw(x, y)
        }
    }

    override fun genAniConfig() {
        config {
            // UnconnectedAni
            From(UnconnectedAni) To DownloadAni When {
                outputItem != null
            } To NoPowerAni When {
                !consValid()
            }
            // BlockedAni
            From(BlockedAni) To UnconnectedAni When {
                outputItem == null
            } To DownloadAni When {
                !isBlocked || lastFullDataDelta < fullTime
            } To NoPowerAni When {
                !consValid()
            }
            // DownloadAni
            From(DownloadAni) To UnconnectedAni When {
                outputItem == null
            } To BlockedAni When {
                isBlocked && lastFullDataDelta > fullTime
            } To NoPowerAni When {
                !consValid()
            }
            // NoPower
            From(NoPowerAni) To UnconnectedAni When {
                consValid() && outputItem == null
            } To DownloadAni When {
                consValid() && outputItem != null
            }
        }
    }
}