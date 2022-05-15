package net.liplum.blocks.data

import arc.graphics.Color
import arc.util.Nullable
import arc.util.Time
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.gen.Building
import mindustry.graphics.Pal
import mindustry.logic.LAccess
import mindustry.type.Item
import mindustry.world.meta.BlockGroup
import net.liplum.*
import net.liplum.api.cyber.*
import net.liplum.blocks.AniedBlock
import net.liplum.blocks.data.Sender.SenderBuild
import net.liplum.lib.Draw
import net.liplum.lib.ResetColor
import net.liplum.lib.SetColor
import net.liplum.lib.animations.anims.Animation
import net.liplum.lib.animations.anis.AniState
import net.liplum.lib.animations.anis.config
import net.liplum.lib.ui.bars.AddBar
import net.liplum.utils.*

private typealias AniStateS = AniState<Sender, SenderBuild>

open class Sender(name: String) : AniedBlock<Sender, SenderBuild>(name) {
    @ClientOnly lateinit var UpArrowTR: TR
    @ClientOnly lateinit var CrossTR: TR
    @ClientOnly lateinit var NoPowerTR: TR
    @ClientOnly lateinit var UnconnectedTR: TR
    @ClientOnly lateinit var UploadAnim: Animation
    @JvmField var UploadAnimFrameNumber = 7
    @JvmField var UploadAnimDuration = 30f
    @ClientOnly @JvmField var SendingTime = 60f
    @JvmField val CheckConnectionTimer = timers++

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
        UpArrowTR = this.inMod("rs-up-arrow")
        CrossTR = this.inMod("rs-cross")
        UnconnectedTR = this.inMod("rs-unconnected")
        NoPowerTR = this.inMod("rs-no-power")
        UploadAnim = this.autoAnimInMod("rs-up-arrow", UploadAnimFrameNumber, UploadAnimDuration)
    }

    override fun setBars() {
        super.setBars()
        DebugOnly {
            addReceiverInfo<SenderBuild>()
            AddBar<SenderBuild>("last-sending",
                { "Last Send: ${lastSendingTime.toInt()}" },
                { Pal.bar },
                { lastSendingTime / SendingTime }
            )
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
        @Serialized
        var receiverPackedPos = -1
            set(value) {
                var curBuild = receiver
                curBuild?.disconnect(this)
                field = value
                curBuild = receiver
                curBuild?.connect(this)
            }

        open fun checkReceiverPos() {
            if (receiverPackedPos == -1) return
            /* For payload
             if (receiverPackedPos.drOrPayload() == null) {
                 receiverPackedPos = -1
             }*/
            if (!receiverPackedPos.dr().exists) {
                receiverPackedPos = -1
            }
        }

        override fun updateTile() {
            // Check connection every second
            if (timer(CheckConnectionTimer, 60f)) {
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
        override fun getConnectedReceiver(): Int? {
            return if (receiverPackedPos == -1) null else receiverPackedPos
        }

        override fun handleItem(source: Building, item: Item) {
            if (!canConsume()) {
                return
            }
            val reb = receiver
            if (reb != null) {
                sendData(reb, item, 1)
                ClientOnly {
                    lastSendingTime = 0f
                }
            }
        }

        val receiver: IDataReceiver?
            get() = if (receiverPackedPos != -1)
            /*For payload
            receiverPackedPos.dr() Or { receiverPackedPos.inPayload() }*/
                receiverPackedPos.dr()
            else
                null

        override fun getSenderColor(): Color {
            val receiver = receiver
            if (receiver != null)
                return if (receiver.isDefaultColor)
                    super.getSenderColor()
                else
                    receiver.receiverColor
            return super.getSenderColor()
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
        override fun onConfigureBuildTapped(other: Building): Boolean {
            if (this == other) {
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
            /*  For payload
            if (other is PayloadConveyor.PayloadConveyorBuild) {
                val payload = other.payload
                if (payload is BuildPayload) {
                    val build = payload.build
                    if (build is IDataReceiver) {
                        deselect()
                        if (build.acceptConnection(this)) {
                            connectSync(other.pos())
                        }
                        return false
                    }
                }
            }*/
            return true
        }

        override fun acceptItem(source: Building, item: Item): Boolean {
            if (!canConsume()) {
                return false
            }
            val reb = receiver
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
        @SendDataPack
        override fun connectSync(receiver: IDataReceiver) {
            val pos = receiver.building.pos()
            if (pos != receiverPackedPos) {
                configure(pos)
            }
        }
        @SendDataPack
        override fun disconnectSync(receiver: IDataReceiver) {
            if (receiver.building.pos() == receiverPackedPos) {
                configure(null)
            }
        }
        /*  For payload
        @SendDataPack
        override fun connectSync(receiver: Int) {
            configure(receiver)
        }
        @SendDataPack
        override fun disconnectSync(receiver: Int) {
            configure(null)
        }*/
        override fun control(type: LAccess, p1: Any?, p2: Double, p3: Double, p4: Double) {
            when (type) {
                LAccess.shoot ->
                    if (p1 is IDataReceiver) connectSync(p1)
                else -> super.control(type, p1, p2, p3, p4)
            }
        }

        override fun control(type: LAccess, p1: Double, p2: Double, p3: Double, p4: Double) {
            when (type) {
                LAccess.shoot -> {
                    val receiver = buildAt(p1, p2)
                    if (receiver is IDataReceiver) connectSync(receiver)
                }
                else -> super.control(type, p1, p2, p3, p4)
            }
        }

        override fun sense(sensor: LAccess): Double {
            return when (sensor) {
                LAccess.shootX -> receiver.tileXd
                LAccess.shootY -> receiver.tileYd
                else -> super.sense(sensor)
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
            UploadAnim.draw(Color.green, x, y)
        }
        BlockedAni = addAniState("Blocked") {
            SetColor(R.C.Stop)
            UpArrowTR.Draw(x, y)
            ResetColor()
        }
        NoPowerAni = addAniState("NoPower") {
            NoPowerTR.Draw(x, y)
        }
    }

    override fun genAniConfig() {
        config {
            // Idle
            From(IdleAni) To UploadAni When {
                val reb = receiver
                reb != null
            } To NoPowerAni When {
                !canConsume()
            }
            // Upload
            From(UploadAni) To IdleAni When {
                receiverPackedPos == -1
            } To BlockedAni When {
                val reb = receiver
                reb != null && isBlocked
            } To NoPowerAni When {
                !canConsume()
            }
            // Blocked
            From(BlockedAni) To IdleAni When {
                receiverPackedPos == -1
            } To UploadAni When {
                val reb = receiver
                reb != null && !isBlocked
            } To NoPowerAni When {
                !canConsume()
            }
            // NoPower
            From(NoPowerAni) To IdleAni When {
                canConsume()
            }
        }
    }
}