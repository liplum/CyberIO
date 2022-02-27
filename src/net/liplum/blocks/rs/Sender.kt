package net.liplum.blocks.rs

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Lines
import arc.math.Mathf
import arc.util.Nullable
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.Vars
import mindustry.gen.Building
import mindustry.type.Item
import mindustry.world.Block
import mindustry.world.Tile
import mindustry.world.meta.BlockGroup
import net.liplum.CioMod
import net.liplum.R
import net.liplum.animations.anims.blocks.AutoAnimation
import net.liplum.animations.anis.AniConfig
import net.liplum.animations.anis.AniState
import net.liplum.api.data.IDataReceiver
import net.liplum.api.data.IDataSender
import net.liplum.blocks.AniedBlock
import net.liplum.blocks.rs.Sender.SenderBuild
import net.liplum.utils.*

private typealias AniStateS = AniState<Sender, SenderBuild>

open class Sender(name: String) : AniedBlock<Sender, SenderBuild>(name) {
    lateinit var CoverTR: TR
    lateinit var UpArrowTR: TR
    lateinit var CrossTR: TR
    lateinit var NoPowerTR: TR
    lateinit var UnconnectedTR: TR
    lateinit var IdleAni: AniStateS
    lateinit var UploadAni: AniStateS
    lateinit var BlockedAni: AniStateS
    lateinit var NoPowerAni: AniStateS
    lateinit var UploadAnim: AutoAnimation
    var UploadAnimFrameNumber = 7
    var UploadAnimDuration = 30f

    init {
        solid = true
        update = true
        acceptsItems = true
        configurable = true
        group = BlockGroup.transportation
        canOverdrive = false
        config(Integer::class.java) { obj: SenderBuild, receiverPackedPos ->
            obj.setReceiverPackedPos(
                receiverPackedPos.toInt()
            )
        }
    }

    override fun genAniState() {
        IdleAni = addAniState("Idle")

        UploadAni = addAniState("Upload") { _, build ->
            UploadAnim.draw(
                Color.green,
                build.x,
                build.y
            )
        }
        BlockedAni = addAniState("Blocked") { sender, build ->
            Draw.color(Color.red)
            Draw.rect(
                sender.UpArrowTR,
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
        aniConfig = AniConfig<Sender, SenderBuild>()
        aniConfig.defaultState(IdleAni)
        // Idle
        aniConfig From IdleAni
        aniConfig To UploadAni When { _, build ->
            val reb = build.receiverBuilding
            reb != null && reb.canAcceptAnyData(build)
        } To BlockedAni When { _, build ->
            val reb = build.receiverBuilding
            reb != null && !reb.isOutputting && !reb.canAcceptAnyData(build)
        } To NoPowerAni When { _, build ->
            Mathf.zero(build.power.status)
        }
        // Upload
        aniConfig From UploadAni
        aniConfig To IdleAni When { _, build ->
            build.getReceiverPackedPos() == -1
        } To BlockedAni When { _, build ->
            val reb = build.receiverBuilding
            reb != null && !reb.isOutputting && !reb.canAcceptAnyData(build)
        } To NoPowerAni When { _, build ->
            Mathf.zero(build.power.status)
        }
        // Blocked
        aniConfig From BlockedAni
        aniConfig To IdleAni When { _, build ->
            build.getReceiverPackedPos() == -1
        } To UploadAni When { _, build ->
            val reb = build.receiverBuilding
            reb != null && (reb.isOutputting || reb.canAcceptAnyData(build))
        } To NoPowerAni When { _, build ->
            Mathf.zero(build.power.status)
        }
        // NoPower
        aniConfig From NoPowerAni
        aniConfig To IdleAni When { _, build ->
            !Mathf.zero(build.power.status)
        } To UploadAni When { _, build ->
            if (Mathf.zero(build.power.status)) {
                return@When false
            }
            val reb = build.receiverBuilding
            reb != null && reb.canAcceptAnyData(build)
        }
        aniConfig.build()
    }

    override fun load() {
        super.load()
        CoverTR = AtlasU.cio("rs-cover")
        UpArrowTR = AtlasU.cio("rs-up-arrow")
        CrossTR = AtlasU.cio("rs-cross")
        UnconnectedTR = AtlasU.cio("rs-unconnected")
        NoPowerTR = AtlasU.cio("rs-no-power")
        loadAnimation()
    }

    fun loadAnimation() {
        UploadAnim = AnimU.autoCio("rs-up-arrow", UploadAnimFrameNumber, UploadAnimDuration)
    }

    override fun setBars() {
        super.setBars()
        if (CioMod.DebugMode) {
            bars.addAniStateInfo<AniedBuild>()
        }
    }

    inner class SenderBuild : AniedBuild(), IDataSender {
        private var receiverPackedPos = -1
        fun getReceiverPackedPos(): Int {
            return receiverPackedPos
        }

        fun setReceiverPackedPos(receiverPackedPos: Int) {
            var curBuild = receiverBuilding
            curBuild?.disconnect(this)
            this.receiverPackedPos = receiverPackedPos
            curBuild = receiverBuilding
            curBuild?.connect(this)
        }

        private fun checkReceiverPos() {
            if (getReceiverPackedPos() != -1 && receiverBuilding == null) {
                setReceiverPackedPos(-1)
            }
        }

        override fun fixedUpdateTile() {
            checkReceiverPos()
        }

        override fun toString(): String {
            return super.toString() + "(receiverPackedPos:" + receiverPackedPos + ")"
        }

        override fun sendData(receiver: IDataReceiver, item: Item, amount: Int) {
            receiver.receiveData(this, item, amount)
        }
        @Nullable
        override fun connectedReceiver(): Int? {
            return if (receiverPackedPos == -1) null else receiverPackedPos
        }

        override fun drawSelect() {
            G.init()
            G.drawSurroundingCircle(tile, R.C.Sender)
            val dr = receiverBuilding
            if (dr != null) {
                val ret = dr.tile
                G.drawSurroundingCircle(ret, R.C.Receiver)
                G.drawDashLineBetweenTwoBlocks(tile, ret, R.C.Sender)
                G.drawArrowBetweenTwoBlocks(tile, ret, R.C.Sender)
            }
        }

        override fun handleItem(source: Building, item: Item) {
            val reb = receiverBuilding
            if (reb != null && !Mathf.zero(power.status)) {
                sendData(reb, item, 1)
            }
        }

        @get:Nullable val receiverBuilding: IDataReceiver?
            get() {
                if (getReceiverPackedPos() != -1) {
                    val rBuild = Vars.world.build(getReceiverPackedPos())
                    if (rBuild is IDataReceiver) {
                        return rBuild
                    }
                }
                return null
            }

        override fun drawConfigure() {
            G.init()
            Lines.stroke(1f)
            G.drawSurroundingCircle(tile, R.C.Sender)
            val dr = receiverBuilding
            if (dr != null) {
                val ret = dr.tile
                G.drawSurroundingCircle(ret, R.C.Receiver)
                G.drawDashLineBetweenTwoBlocks(tile, ret, R.C.Sender)
                G.drawArrowBetweenTwoBlocks(tile, ret, R.C.Sender)
            }
        }

        fun clearReceiver() {
            configure(-1)
        }

        override fun onConfigureTileTapped(other: Building): Boolean {
            if (this === other) {
                deselect()
                clearReceiver()
                return false
            }
            if (getReceiverPackedPos() == other.pos()) {
                deselect()
                clearReceiver()
                return false
            }
            if (other is IDataReceiver) {
                deselect()
                val receiver = other as IDataReceiver
                if (receiver.acceptConnection(this)) {
                    setReceiver(receiver)
                }
                return false
            }
            return true
        }

        fun setReceiver(receiver: IDataReceiver) {
            configure(receiver.building.pos())
        }

        override fun acceptItem(source: Building, item: Item): Boolean {
            if (Mathf.zero(power.status)) {
                return false
            }
            val reb = receiverBuilding
            return reb?.acceptData(this, item) ?: false
        }

        override fun write(write: Writes) {
            super.write(write)
            write.i(receiverPackedPos)
        }

        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            receiverPackedPos = read.i()
        }

        override fun config(): Any {
            return getReceiverPackedPos()
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