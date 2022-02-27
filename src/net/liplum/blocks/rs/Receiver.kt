package net.liplum.blocks.rs

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.math.Mathf
import arc.scene.ui.layout.Table
import arc.struct.ObjectSet
import arc.struct.OrderedSet
import arc.util.Eachable
import arc.util.Nullable
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
import net.liplum.R
import net.liplum.animations.anims.IAnimatedBlock
import net.liplum.animations.anis.AniConfig
import net.liplum.animations.anis.AniState
import net.liplum.api.data.IDataReceiver
import net.liplum.api.data.IDataSender
import net.liplum.blocks.AniedBlock
import net.liplum.blocks.rs.Receiver.ReceiverBuild
import net.liplum.utils.*

private typealias AniStateR = AniState<Receiver, ReceiverBuild>

open class Receiver(name: String?) : AniedBlock<Receiver, ReceiverBuild>(name) {
    lateinit var CoverTR: TR
    lateinit var DownArrowTR: TR
    lateinit var UnconnectedTR: TR
    lateinit var NoPowerTR: TR
    lateinit var DownloadAni: AniStateR
    lateinit var UnconnectedAni: AniStateR
    lateinit var BlockedAni: AniStateR
    lateinit var NoPowerAni: AniStateR
    lateinit var DownloadAnim: IAnimatedBlock
    var maxConnection = -1
    var DownloadAnimFrameNumber = 7
    var DownloadAnimDuration = 30f

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
            selected.block, selectedTile.x.toInt(), selectedTile.y.toInt(),
            this, x, y,
            R.C.Sender
        )
        G.drawArrowBetweenTwoBlocks(
            selected.block, selectedTile.x.toInt(), selectedTile.y.toInt(), this, x, y,
            R.C.Sender
        )
    }

    override fun genAniState() {
        DownloadAni = addAniState("Download") { _, build ->
            if (build.outputItem != null) {
                DownloadAnim.draw(Color.green, build.x, build.y)
            }
        }
        UnconnectedAni = addAniState("Unconnected") { sender, build ->
            Draw.color(Color.white)
            Draw.rect(
                sender.UnconnectedTR,
                build.x, build.y
            )
            Draw.color()
        }
        BlockedAni = addAniState("Blocked") { sender, build ->
            Draw.color(Color.red)
            Draw.rect(
                sender.DownArrowTR,
                build.x, build.y
            )
            Draw.color()
        }
        NoPowerAni = addAniState("NoPower") { sender, build ->
            Draw.rect(
                sender.NoPowerTR,
                build.x, build.y
            )
        }
    }

    override fun genAniConfig() {
        aniConfig = AniConfig<Receiver, ReceiverBuild>()
        aniConfig.defaultState(UnconnectedAni)
        // UnconnectedAni
        aniConfig From UnconnectedAni
        aniConfig To DownloadAni When { _, build ->
            build.outputItem != null
        } To NoPowerAni When { _, build ->
            Mathf.zero(
                build.power.status
            )
        }
        // BlockedAni
        aniConfig From BlockedAni
        aniConfig To UnconnectedAni When { _, build ->
            build.outputItem == null
        } To DownloadAni When { _, build ->
            build.isOutputting || build.lastFullDataDelta < 60
        } To NoPowerAni When { _, build ->
            Mathf.zero(
                build.power.status
            )
        }
        // DownloadAni
        aniConfig From DownloadAni
        aniConfig To UnconnectedAni When { _, build ->
            build.outputItem == null
        } To BlockedAni When { _, build ->
            !build.isOutputting && build.lastFullDataDelta > 60
        } To NoPowerAni When { _, build ->
            Mathf.zero(
                build.power.status
            )
        }
        // NoPower
        aniConfig From NoPowerAni
        aniConfig To UnconnectedAni When { _, build ->
            !Mathf.zero(
                build.power.status
            ) && build.outputItem == null
        } To DownloadAni When { _, build ->
            !Mathf.zero(
                build.power.status
            ) && build.outputItem != null
        }
        aniConfig.build()
    }

    override fun load() {
        super.load()
        CoverTR = AtlasU.cio("rs-cover")
        DownArrowTR = AtlasU.cio("rs-down-arrow")
        UnconnectedTR = AtlasU.cio("rs-unconnected")
        NoPowerTR = AtlasU.cio("rs-no-power")
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

    override fun outputsItems(): Boolean {
        return true
    }

    inner class ReceiverBuild : AniedBuild(), IDataReceiver {
        @get:Nullable
        var outputItem: Item? = null
        private var isOutputting = false
        private var lastOutputDelta = 0f
        var lastFullDataDelta = 0f
        var sendersPos = OrderedSet<Int>()
        override fun isOutputting(): Boolean {
            return lastOutputDelta < 30f
        }

        override fun connect(sender: IDataSender) {
            sendersPos.add(sender.building.pos())
        }

        override fun disconnect(sender: IDataSender) {
            sendersPos.remove(sender.building.pos())
        }

        fun checkSenderPos() {
            val it = sendersPos.iterator()
            while (it.hasNext) {
                val curSenderPos: Int = it.next()
                val sBuild = Vars.world.build(curSenderPos)
                if (sBuild !is IDataSender) {
                    it.remove()
                }
            }
        }

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
            CyberU.drawSenders(this, sendersPos)
        }

        override fun fixedUpdateTile() {
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
            if (!Mathf.zero(power.status) && outputItem != null) {
                if (dump(outputItem)) {
                    lastOutputDelta = 0f
                } else {
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

        override fun acceptItem(source: Building, item: Item): Boolean {
            return false
        }

        override fun acceptData(source: IDataSender, item: Item): Boolean {
            return items[item] < getMaximumAccepted(item) &&
                    outputItem === item
        }

        override fun canAcceptAnyData(sender: IDataSender): Boolean {
            val outputItem = outputItem ?: return false
            return items[outputItem] < getMaximumAccepted(outputItem)
        }

        override fun receiveData(sender: IDataSender, item: Item, amount: Int) {
            items.add(item, 1)
        }

        override fun config(): Item {
            return (outputItem)!!
        }

        override fun write(write: Writes) {
            super.write(write)
            write.s(if (outputItem == null) -1 else outputItem!!.id.toInt())
            write.bool(isOutputting)
            RWU.writeIntSet(write, sendersPos)
        }

        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            outputItem = Vars.content.item(read.s().toInt())
            isOutputting = read.bool()
            sendersPos = RWU.readIntSet(read)
        }

        override fun connectedSenders(): ObjectSet<Int> {
            return sendersPos
        }

        override fun connectedSender(): Int? {
            return sendersPos.first()
        }

        override fun acceptConnection(sender: IDataSender): Boolean {
            return if (maxConnection == -1) {
                true
            } else {
                sendersPos.size < maxConnection
            }
        }

        override fun getBuilding(): Building {
            return this
        }

        override fun getTile(): Tile {
            return tile()
        }

        override fun getBlock(): Block {
            return block()
        }
    }
}