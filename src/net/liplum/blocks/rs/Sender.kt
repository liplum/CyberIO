package net.liplum.blocks.rs

import arc.graphics.Color
import arc.util.Nullable
import arc.util.Time
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.gen.Building
import mindustry.graphics.Pal
import mindustry.type.Item
import mindustry.ui.Bar
import mindustry.world.Block
import mindustry.world.Tile
import mindustry.world.meta.BlockGroup
import net.liplum.*
import net.liplum.animations.anims.Animation
import net.liplum.animations.anis.*
import net.liplum.api.data.*
import net.liplum.blocks.AniedBlock
import net.liplum.blocks.rs.Sender.SenderBuild
import net.liplum.utils.AnimU
import net.liplum.utils.TR
import net.liplum.utils.addReceiverInfo
import net.liplum.utils.inMod

private typealias AniStateS = AniState<Sender, SenderBuild>

open class Sender(name: String) : AniedBlock<Sender, SenderBuild>(name) {
    @ClientOnly lateinit var CoverTR: TR
    @ClientOnly lateinit var UpArrowTR: TR
    @ClientOnly lateinit var CrossTR: TR
    @ClientOnly lateinit var NoPowerTR: TR
    @ClientOnly lateinit var UnconnectedTR: TR
    @ClientOnly lateinit var UploadAnim: Animation
    @JvmField var UploadAnimFrameNumber = 7
    @JvmField var UploadAnimDuration = 30f
    @ClientOnly @JvmField var SendingTime = 60f

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
            it.receiverPackedPos = -1
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
        DebugOnly {
            bars.addReceiverInfo<SenderBuild>()
            bars.add<SenderBuild>("last-sending") {
                Bar(
                    { "Last Send: ${it.lastSendingTime.toInt()}" },
                    { Pal.bar },
                    { it.lastSendingTime / SendingTime }
                )
            }
        }
    }

    open inner class SenderBuild : AniedBuild(), IDataSender {
        @ClientOnly var lastSendingTime = 0f
            set(value) {
                field = value.coerceAtLeast(0f)
            }
        @ClientOnly
        open val isBlocked: Boolean
            get() = lastSendingTime > SendingTime
        @set:CalledBySync
        var receiverPackedPos = -1
            set(value) {
                var curBuild = receiverBuilding
                curBuild?.disconnect(this)
                field = value
                curBuild = receiverBuilding
                curBuild?.connect(this)
            }

        open fun checkReceiverPos() {
            if (!receiverPackedPos.dr().exists) {
                receiverPackedPos = -1
            }
        }

        override fun updateTile() {
            // Check connection every second
            if (Time.time % 60f < 1) {
                checkReceiverPos()
            }
            ClientOnly {
                lastSendingTime += Time.delta
            }
        }

        override fun toString(): String {
            return super.toString() + "(receiverPackedPos:" + receiverPackedPos + ")"
        }
        @Nullable
        override fun connectedReceiver(): Int? {
            return if (receiverPackedPos == -1) null else receiverPackedPos
        }

        override fun handleItem(source: Building, item: Item) {
            if (!consValid()) {
                return
            }
            val reb = receiverBuilding
            if (reb != null) {
                sendData(reb, item, 1)
                ClientOnly {
                    lastSendingTime = 0f
                }
            }
        }

        val receiverBuilding: IDataReceiver?
            get() {
                if (receiverPackedPos != -1) {
                    return receiverPackedPos.dr()
                }
                return null
            }
        @ClientOnly
        override fun drawConfigure() {
            super.drawConfigure()
            this.drawDataNetGraphic()
        }
        @ClientOnly
        override fun drawSelect() {
            this.drawDataNetGraphic()
        }

        @ClientOnly
        override fun onConfigureTileTapped(other: Building): Boolean {
            if (this === other) {
                deselect()
                configure(null)
                return false
            }
            if (receiverPackedPos == other.pos()) {
                deselect()
                configure(null)
                return false
            }
            if (other is IDataReceiver) {
                deselect()
                if (other.acceptConnection(this)) {
                    connectSync(other)
                }
                return false
            }
            return true
        }

        override fun acceptItem(source: Building, item: Item): Boolean {
            if (!consValid()) {
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

        override fun getBuilding(): Building = this
        override fun getTile(): Tile = tile
        override fun getBlock(): Block = this@Sender
        @SendDataPack
        override fun connectSync(receiver: IDataReceiver) {
            configure(receiver.building.pos())
        }
        @SendDataPack
        override fun disconnectSync(receiver: IDataReceiver) {
            if (receiver.building.pos() == receiverPackedPos) {
                configure(null)
            }
        }
    }

    @ClientOnly lateinit var IdleAni: AniStateS
    @ClientOnly lateinit var UploadAni: AniStateS
    @ClientOnly lateinit var BlockedAni: AniStateS
    @ClientOnly lateinit var NoPowerAni: AniStateS
    override fun genAniState() {
        IdleAni = addAniState("Idle")

        UploadAni = addAniState("Upload") {
            UploadAnim.draw(Color.green, it.x, it.y)
        }
        BlockedAni = addAniState("Blocked") {
            SetColor(R.C.Stop)
            DrawTR(UpArrowTR, it.x, it.y)
            ResetColor()
        }
        NoPowerAni = addAniState("NoPower") {
            DrawTR(NoPowerTR, it.x, it.y)
        }
    }

    override fun genAniConfig() {
        config {
            // Idle
            From(IdleAni) To UploadAni When {
                val reb = it.receiverBuilding
                reb != null
            } To NoPowerAni When {
                !it.consValid()
            }
            // Upload
            From(UploadAni) To IdleAni When {
                it.receiverPackedPos == -1
            } To BlockedAni When {
                val reb = it.receiverBuilding
                reb != null && it.isBlocked
            } To NoPowerAni When {
                !it.consValid()
            }
            // Blocked
            From(BlockedAni) To IdleAni When {
                it.receiverPackedPos == -1
            } To UploadAni When {
                val reb = it.receiverBuilding
                reb != null && !it.isBlocked
            } To NoPowerAni When {
                !it.consValid()
            }
            // NoPower
            From(NoPowerAni) To IdleAni When {
                it.consValid()
            }
        }
    }
}