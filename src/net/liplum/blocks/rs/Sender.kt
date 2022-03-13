package net.liplum.blocks.rs

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Lines
import arc.util.Nullable
import arc.util.Time
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.Vars
import mindustry.gen.Building
import mindustry.type.Item
import mindustry.world.Block
import mindustry.world.Tile
import mindustry.world.meta.BlockGroup
import net.liplum.CioMod
import net.liplum.ClientOnly
import net.liplum.R
import net.liplum.animations.anims.Animation
import net.liplum.animations.anis.AniState
import net.liplum.animations.anis.config
import net.liplum.api.data.CyberU
import net.liplum.api.data.IDataReceiver
import net.liplum.api.data.IDataSender
import net.liplum.api.data.isAccepted
import net.liplum.blocks.AniedBlock
import net.liplum.blocks.rs.Sender.SenderBuild
import net.liplum.utils.*

private typealias AniStateS = AniState<Sender, SenderBuild>

open class Sender(name: String) : AniedBlock<Sender, SenderBuild>(name) {
    @ClientOnly lateinit var CoverTR: TR
    @ClientOnly lateinit var UpArrowTR: TR
    @ClientOnly lateinit var CrossTR: TR
    @ClientOnly lateinit var NoPowerTR: TR
    @ClientOnly lateinit var UnconnectedTR: TR
    @ClientOnly lateinit var IdleAni: AniStateS
    @ClientOnly lateinit var UploadAni: AniStateS
    @ClientOnly lateinit var BlockedAni: AniStateS
    @ClientOnly lateinit var NoPowerAni: AniStateS
    @ClientOnly lateinit var UploadAnim: Animation
    @JvmField var UploadAnimFrameNumber = 7
    @JvmField var UploadAnimDuration = 30f

    init {
        solid = true
        update = true
        acceptsItems = true
        configurable = true
        group = BlockGroup.transportation
        canOverdrive = false
        unloadable = false
        config(Integer::class.java) { obj: SenderBuild, receiverPackedPos ->
            obj.receiverPackedPos = receiverPackedPos.toInt()
        }
        configClear<SenderBuild> {
            it.clearReceiver()
        }
    }

    override fun load() {
        super.load()
        CoverTR = this.inMod("rs-cover")
        UpArrowTR = this.inMod("rs-up-arrow")
        CrossTR = this.inMod("rs-cross")
        UnconnectedTR = this.inMod("rs-unconnected")
        NoPowerTR = this.inMod("rs-no-power")
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

    open inner class SenderBuild : AniedBuild(), IDataSender {
        var receiverPackedPos = -1
            set(value) {
                var curBuild = receiverBuilding
                curBuild?.disconnect(this)
                field = value
                curBuild = receiverBuilding
                curBuild?.connect(this)
            }

        open fun checkReceiverPos() {
            if (receiverPackedPos != -1 && receiverBuilding == null) {
                receiverPackedPos = -1
            }
        }

        override fun updateTile() {
            // Check connection every second
            if (Time.time % 60f < 1) {
                checkReceiverPos()
            }
        }

        override fun toString(): String {
            return super.toString() + "(receiverPackedPos:" + receiverPackedPos + ")"
        }
        @Nullable
        override fun connectedReceiver(): Int? {
            return if (receiverPackedPos == -1) null else receiverPackedPos
        }

        override fun drawSelect() {
            G.init()
            G.drawSurroundingCircle(tile, R.C.Sender)

            CyberU.drawReceiver(this, receiverPackedPos)
        }

        override fun handleItem(source: Building, item: Item) {
            val reb = receiverBuilding
            if (reb != null && !power.status.isZero()) {
                sendData(reb, item, 1)
            }
        }
        @get:Nullable
        val receiverBuilding: IDataReceiver?
            get() {
                if (receiverPackedPos != -1) {
                    val rBuild = Vars.world.build(receiverPackedPos)
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
            CyberU.drawReceiver(this, receiverPackedPos)
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
            if (receiverPackedPos == other.pos()) {
                deselect()
                clearReceiver()
                return false
            }
            if (other is IDataReceiver) {
                deselect()
                if (other.acceptConnection(this)) {
                    setReceiver(other)
                }
                return false
            }
            return true
        }

        fun setReceiver(receiver: IDataReceiver) {
            configure(receiver.building.pos())
        }

        override fun acceptItem(source: Building, item: Item): Boolean {
            if (power.status.isZero()) {
                return false
            }
            val reb = receiverBuilding
            return reb?.acceptedAmount(this, item)?.isAccepted() ?: false
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
            return receiverPackedPos
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

        override fun connect(receiver: IDataReceiver) {
            setReceiver(receiver)
        }

        override fun disconnect(receiver: IDataReceiver) {
            clearReceiver()
        }
    }

    override fun genAniState() {
        IdleAni = addAniState("Idle")

        UploadAni = addAniState("Upload") {
            UploadAnim.draw(
                Color.green,
                it.x,
                it.y
            )
        }
        BlockedAni = addAniState("Blocked") {
            Draw.color(Color.red)
            Draw.rect(
                UpArrowTR,
                it.x, it.y
            )
            Draw.color()
        }
        NoPowerAni = addAniState("NoPower") {
            Draw.rect(
                NoPowerTR,
                it.x, it.y
            )
        }
    }

    override fun genAniConfig() {
        config {
            // Idle
            From(IdleAni) To UploadAni When {
                val reb = it.receiverBuilding
                reb != null && reb.canAcceptAnyData(it)
            } To BlockedAni When {
                val reb = it.receiverBuilding
                reb != null && (reb.isBlocked || !reb.canAcceptAnyData(it))
            } To NoPowerAni When {
                it.power.status.isZero()
            }
            // Upload
            From(UploadAni) To IdleAni When {
                it.receiverPackedPos == -1
            } To BlockedAni When {
                val reb = it.receiverBuilding
                reb != null && (reb.isBlocked || !reb.canAcceptAnyData(it))
            } To NoPowerAni When {
                it.power.status.isZero()
            }
            // Blocked
            From(BlockedAni) To IdleAni When {
                it.receiverPackedPos == -1
            } To UploadAni When {
                val reb = it.receiverBuilding
                reb != null && (!reb.isBlocked || reb.canAcceptAnyData(it))
            } To NoPowerAni When {
                it.power.status.isZero()
            }
            // NoPower
            From(NoPowerAni) To IdleAni When {
                !it.power.status.isZero()
            } To UploadAni When {
                if (it.power.status.isZero()) {
                    false
                } else {
                    val reb = it.receiverBuilding
                    reb != null && reb.canAcceptAnyData(it)
                }
            }
        }
    }
}