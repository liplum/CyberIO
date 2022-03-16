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
import net.liplum.animations.anis.DrawTR
import net.liplum.animations.anis.SetColor
import net.liplum.animations.anis.config
import net.liplum.api.data.*
import net.liplum.blocks.AniedBlock
import net.liplum.blocks.rs.Receiver.ReceiverBuild
import net.liplum.delegates.Delegate1
import net.liplum.persistance.intSet
import net.liplum.ui.bars.removeItems
import net.liplum.utils.AnimU
import net.liplum.utils.TR
import net.liplum.utils.addSenderInfo
import net.liplum.utils.inMod

private typealias AniStateR = AniState<Receiver, ReceiverBuild>

open class Receiver(name: String) : AniedBlock<Receiver, ReceiverBuild>(name) {
    @ClientOnly lateinit var CoverTR: TR
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
        CoverTR = this.inMod("rs-cover")
        DownArrowTR = this.inMod("rs-down-arrow")
        UnconnectedTR = this.inMod("rs-unconnected")
        NoPowerTR = this.inMod("rs-no-power")
        loadAnimation()
    }

    fun loadAnimation() {
        DownloadAnim = AnimU.autoCio("rs-down-arrow", DownloadAnimFrameNumber, DownloadAnimDuration)
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
            val outputItem = outputItem
            whenNotConfiguringSender {
                this.drawDataNetGraphic()
            }
            if (outputItem != null) {
                val dx = x - size * Vars.tilesize / 2f
                val dy = y + size * Vars.tilesize / 2f
                Draw.mixcol(Color.darkGray, 1f)
                Draw.rect(outputItem.uiIcon, dx, dy - 1)
                Draw.reset()
                Draw.rect(outputItem.uiIcon, dx, dy)
            }
        }

        override fun updateTile() {
            // Check connection every second
            if (Time.time % 60f < 1) {
                checkSenderPos()
            }
            val outputItem = outputItem
            val deltaT = Time.delta
            if (outputItem != null) {
                ClientOnly {
                    val isFullData = items[outputItem] < getMaximumAccepted(outputItem)
                    if (isFullData) {
                        lastFullDataDelta = 0f
                    } else {
                        lastFullDataDelta += deltaT
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
                    lastOutputDelta += deltaT
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
            items.add(item, amount)
        }

        override fun getRequirements() = outputItem.req
        @ClientOnly
        override fun canAcceptAnyData(sender: IDataSender): Boolean {
            if (!consValid()) return false
            val outputItem = outputItem ?: return false
            return items[outputItem] < getMaximumAccepted(outputItem)
        }

        override fun config(): Item? = outputItem
        override fun write(write: Writes) {
            super.write(write)
            write.s(if (outputItem == null) -1 else outputItem!!.id.toInt())
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
        override fun connectedSender(): Int? = senders.first()
        override fun acceptConnection(sender: IDataSender) =
            if (maxConnection == -1) true else senders.size < maxConnection

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
            DownloadAnim.draw(Color.green, it.x, it.y)
        }
        UnconnectedAni = addAniState("Unconnected") {
            SetColor(R.C.Unconnected)
            DrawTR(UnconnectedTR, it.x, it.y)
            Draw.color()
        }
        BlockedAni = addAniState("Blocked") {
            SetColor(R.C.Stop)
            Draw.rect(DownArrowTR, it.x, it.y)
            Draw.color()
        }
        NoPowerAni = addAniState("NoPower") {
            Draw.rect(NoPowerTR, it.x, it.y)
        }
    }

    override fun genAniConfig() {
        config {
            // UnconnectedAni
            From(UnconnectedAni) To DownloadAni When {
                it.outputItem != null
            } To NoPowerAni When {
                !it.consValid()
            }
            // BlockedAni
            From(BlockedAni) To UnconnectedAni When {
                it.outputItem == null
            } To DownloadAni When {
                !it.isBlocked || it.lastFullDataDelta < fullTime
            } To NoPowerAni When {
                !it.consValid()
            }
            // DownloadAni
            From(DownloadAni) To UnconnectedAni When {
                it.outputItem == null
            } To BlockedAni When {
                it.isBlocked && it.lastFullDataDelta > fullTime
            } To NoPowerAni When {
                !it.consValid()
            }
            // NoPower
            From(NoPowerAni) To UnconnectedAni When {
                it.consValid() && it.outputItem == null
            } To DownloadAni When {
                it.consValid() && it.outputItem != null
            }
        }
    }
}