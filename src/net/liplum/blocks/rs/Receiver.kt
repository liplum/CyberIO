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
import net.liplum.CioMod
import net.liplum.ClientOnly
import net.liplum.R
import net.liplum.animations.anims.Animation
import net.liplum.animations.anis.AniState
import net.liplum.animations.anis.config
import net.liplum.api.data.*
import net.liplum.blocks.AniedBlock
import net.liplum.blocks.rs.Receiver.ReceiverBuild
import net.liplum.persistance.intSet
import net.liplum.utils.*

private typealias AniStateR = AniState<Receiver, ReceiverBuild>

open class Receiver(name: String) : AniedBlock<Receiver, ReceiverBuild>(name) {
    @ClientOnly lateinit var CoverTR: TR
    @ClientOnly lateinit var DownArrowTR: TR
    @ClientOnly lateinit var UnconnectedTR: TR
    @ClientOnly lateinit var NoPowerTR: TR
    @ClientOnly lateinit var DownloadAni: AniStateR
    @ClientOnly lateinit var UnconnectedAni: AniStateR
    @ClientOnly lateinit var BlockedAni: AniStateR
    @ClientOnly lateinit var NoPowerAni: AniStateR
    @ClientOnly lateinit var DownloadAnim: Animation
    @JvmField var maxConnection = -1
    @JvmField var DownloadAnimFrameNumber = 7
    @JvmField var DownloadAnimDuration = 30f

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
        config(
            Item::class.java
        ) { obj: ReceiverBuild, item ->
            obj.outputItem = item
        }
        configClear { tile: ReceiverBuild -> tile.outputItem = null }
    }

    override fun drawPlace(x: Int, y: Int, rotation: Int, valid: Boolean) {
        super.drawPlace(x, y, rotation, valid)
        if (!Vars.control.input.frag.config.isShown) return
        val selected = Vars.control.input.frag.config.selectedTile
        if (selected == null ||
            selected.block !is Sender
        ) {
            return
        }
        G.init()
        val selectedTile = selected.tile()
        G.drawDashLineBetweenTwoBlocks(
            selected.block, selectedTile.x, selectedTile.y,
            this, x.toShort(), y.toShort(),
            R.C.Sender
        )
        G.drawArrowBetweenTwoBlocks(
            selected.block, selectedTile.x, selectedTile.y, this, x.toShort(), y.toShort(),
            R.C.Sender
        )
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
        if (CioMod.DebugMode) {
            bars.addAniStateInfo<AniedBuild>()
        }
    }

    override fun drawRequestConfig(req: BuildPlan, list: Eachable<BuildPlan>) {
        drawRequestConfigCenter(req, req.config, "center", true)
    }

    override fun outputsItems(): Boolean = true
    open inner class ReceiverBuild : AniedBuild(), IDataReceiver {
        var outputItem: Item? = null
        @ClientOnly
        private var lastOutputDelta = 0f
        @ClientOnly
        var lastFullDataDelta = 0f
        var senders = OrderedSet<Int>()
        open fun checkSenderPos() {
            senders.removeAll { !it.ds().exists }
        }
        @ClientOnly
        override fun isBlocked(): Boolean = lastOutputDelta > 30f
        override fun drawSelect() {
            val outputItem = outputItem
            G.init()
            G.drawSurroundingCircle(tile, R.C.Receiver)
            if (outputItem != null) {
                val dx = x - size * Vars.tilesize / 2f
                val dy = y + size * Vars.tilesize / 2f
                Draw.mixcol(Color.darkGray, 1f)
                Draw.rect(outputItem.uiIcon, dx, dy - 1)
                Draw.reset()
                Draw.rect(outputItem.uiIcon, dx, dy)
            }
            CyberU.drawSenders(this, senders)
        }

        override fun updateTile() {
            // Check connection every second
            if (Time.time % 60f < 1) {
                checkSenderPos()
            }
            val outputItem = outputItem
            val deltaT = Time.delta
            if (outputItem != null) {
                val isFullData = items[outputItem] < getMaximumAccepted(outputItem)
                if (isFullData) {
                    lastFullDataDelta = 0f
                } else {
                    lastFullDataDelta += deltaT
                }
            }
            ClientOnly {
                if (!power.status.isZero() && outputItem != null) {
                    if (dump(outputItem)) {
                        lastOutputDelta = 0f
                    } else {
                        lastOutputDelta += deltaT
                    }
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
        override fun acceptedAmount(sender: IDataSender, itme: Item) =
            if (itme == outputItem)
                getMaximumAccepted(outputItem) - items[outputItem]
            else
                0

        override fun receiveData(sender: IDataSender, item: Item, amount: Int) {
            items.add(item, amount)
        }

        override fun getRequirements() = outputItem.req
        @ClientOnly
        override fun canAcceptAnyData(sender: IDataSender): Boolean {
            val outputItem = outputItem ?: return false
            return items[outputItem] < getMaximumAccepted(outputItem) && !isBlocked
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

        override fun connect(sender: IDataSender) {
            senders.add(sender.building.pos())
        }

        override fun disconnect(sender: IDataSender) {
            senders.remove(sender.building.pos())
        }

        override fun connectedSenders() = senders
        override fun connectedSender(): Int? = senders.first()
        override fun acceptConnection(sender: IDataSender) =
            if (maxConnection == -1) true else senders.size < maxConnection

        override fun getBuilding(): Building = this
        override fun getTile(): Tile = tile()
        override fun getBlock(): Block = block()
    }

    override fun genAniState() {
        DownloadAni = addAniState("Download") {
            if (it.outputItem != null) {
                DownloadAnim.draw(Color.green, it.x, it.y)
            }
        }
        UnconnectedAni = addAniState("Unconnected") {
            Draw.color(Color.white)
            Draw.rect(UnconnectedTR, it.x, it.y)
            Draw.color()
        }
        BlockedAni = addAniState("Blocked") {
            Draw.color(Color.red)
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
                it.power.status.isZero()
            }
            // BlockedAni
            From(BlockedAni) To UnconnectedAni When {
                it.outputItem == null
            } To DownloadAni When {
                !it.isBlocked || it.lastFullDataDelta < 60
            } To NoPowerAni When {
                it.power.status.isZero()
            }
            // DownloadAni
            From(DownloadAni) To UnconnectedAni When {
                it.outputItem == null
            } To BlockedAni When {
                it.isBlocked && it.lastFullDataDelta > 60
            } To NoPowerAni When {
                it.power.status.isZero()
            }
            // NoPower
            From(NoPowerAni) To UnconnectedAni When {
                !it.power.status.isZero() && it.outputItem == null
            } To DownloadAni When {
                !it.power.status.isZero() && it.outputItem != null
            }
        }
    }
}