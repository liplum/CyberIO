package net.liplum.blocks.stream

import arc.func.Prov
import arc.graphics.g2d.Draw
import arc.math.Angles
import arc.util.Time
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.gen.Building
import mindustry.graphics.Layer
import mindustry.type.Liquid
import mindustry.world.blocks.liquid.LiquidBlock
import mindustry.world.meta.BlockGroup
import net.liplum.DebugOnly
import net.liplum.Var
import net.liplum.Var.YinYangRotationSpeed
import net.liplum.api.cyber.*
import net.liplum.blocks.AniedBlock
import net.liplum.common.utils.DrawLayer
import net.liplum.lib.Serialized
import net.liplum.lib.assets.EmptyTR
import net.liplum.mdt.CalledBySync
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.SendDataPack
import net.liplum.mdt.animations.anis.AniState
import net.liplum.mdt.animations.anis.config
import net.liplum.mdt.render.Draw
import net.liplum.mdt.render.DrawOn
import net.liplum.mdt.render.Text
import net.liplum.mdt.utils.PackedPos
import net.liplum.mdt.utils.inMod
import net.liplum.mdt.utils.sub
import kotlin.math.absoluteValue

private typealias AniStateP = AniState<P2pNode, P2pNode.P2pBuild>

open class P2pNode(name: String) : AniedBlock<P2pNode, P2pNode.P2pBuild>(name) {
    @ClientOnly var liquidPadding = 0f
    @ClientOnly var NoPowerTR = EmptyTR
    @JvmField var balancingSpeed = 1f
    @JvmField var dumpScale = 2f
    /**
     * The max range when trying to connect. -1f means no limit.
     */
    @JvmField var maxRange = -1f
    @ClientOnly @JvmField var maxSelectedCircleTime = Var.SelectedCircleTime
    @ClientOnly var BottomTR = EmptyTR
    @ClientOnly var YinAndYangTR = EmptyTR

    init {
        buildType = Prov { P2pBuild() }
        hasLiquids = true
        update = true
        solid = true
        group = BlockGroup.liquids
        outputsLiquid = true
        configurable = true
        noUpdateDisabled = true
        callDefaultBlockDraw = false
        canOverdrive = false
        sync = true
        config(java.lang.Integer::class.java) { b: P2pBuild, pos ->
            b.connectedPos = pos.toInt()
        }
        configClear<P2pBuild> {
            it.connected?.connectedPos = -1
            it.connectedPos = -1
        }
    }

    override fun load() {
        super.load()
        NoPowerTR = this.inMod("rs-no-power")
        BottomTR = this.sub("bottom")
        YinAndYangTR = this.sub("yin-and-yang")
    }

    override fun icons() = arrayOf(BottomTR, region, YinAndYangTR)
    open inner class P2pBuild : AniedBuild(), IP2pNode {
        override val maxRange = this@P2pNode.maxRange
        override val currentFluid: Liquid
            get() = liquids.current()
        override val currentAmount: Float
            get() = liquids.currentAmount()
        @Serialized
        @set:CalledBySync
        override var connectedPos: PackedPos = -1
        @ClientOnly
        override var isDrawer: Boolean = false
        @ClientOnly
        override var status = P2pStatus.None
        @SendDataPack(["connectedPos::set"])
        override fun connectToSync(other: IP2pNode) {
            configure(other.building.pos())
        }

        override fun disconnectFromAnotherSync() {
            configure(null)
        }
        /**
         * ### If unconnected
         * limit:
         * 1. Does this match the input?
         * 2. If 1 is false, is this almost empty?
         * 3. Can this hold more fluid?
         * ### If connected
         * limit:
         * 1. Does another match the input?
         * 2. If 1 is false, is another almost empty?
         * 3. Does this match the input?
         * 3. If 3 is false, is this almost empty?
         * 4. Can this hold more fluid?
         */
        override fun acceptLiquid(source: Building, fluid: Liquid): Boolean {
            val connected = connected
            return if (connected == null)
                (currentFluid == fluid || currentAmount < 0.2f) && liquids[fluid] < liquidCapacity
            else
                (connected.currentFluid == fluid || connected.currentAmount < 0.2f) &&
                        (this.currentFluid == fluid || this.currentAmount < 0.2f) &&
                        this.liquids[fluid] < liquidCapacity
        }

        fun balanceFluid() {
            val other = connected
            run {
                if (other != null) {
                    if (this.currentFluid != other.currentFluid && other.currentAmount > 0.02f) return@run
                    val difference = this.currentAmount - other.currentAmount
                    val connection =
                        when {
                            difference > 0f -> this to other //this > other, other needs more
                            difference < 0f -> other to this //this < other, this needs more
                            else -> return@run
                        }
                    val (sender, receiver) = connection
                    val abs = difference.absoluteValue
                    if (abs > Var.P2PNNodeBalanceThreshold) {
                        sender.status = P2pStatus.Sender
                        receiver.status = P2pStatus.Receiver
                        val data = abs
                            .coerceAtMost(sender.currentAmount)
                            .coerceAtMost(receiver.restRoom)
                            .coerceAtMost(balancingSpeed) * delta()
                        if (data > 0.0001f) {
                            sender.streamToAnother(data)
                        }
                    }
                }
            }
        }

        override fun updateTile() {
            balanceFluid()
            if (currentAmount > 0.0001f && timer(timerDump, 1f)) {
                dumpLiquid(currentFluid, dumpScale)
            }
        }

        override fun draw() {
            super.draw()
            val other = connected
            DebugOnly {
                if (other != null) {
                    DrawLayer(Layer.blockOver) {
                        Text.drawTextEasy("(${other.tileX},${other.tileY})", x, y + 10f)
                    }
                }
            }
            if (other != null && isDrawer) {
                val sender: Building
                val receiver: Building
                if (status == P2pStatus.Sender) {
                    sender = this
                    receiver = other.building
                } else {
                    sender = other.building
                    receiver = this
                }
                Draw.z(Layer.blockOver)
                transferArrowLineBreath(
                    sender, receiver,
                    arrowColor = currentFluid.color,
                    density = ArrowDensity,
                    speed = ArrowSpeed,
                    alphaMultiplier = 0.8f
                )
            }
        }
        @ClientOnly
        var yinYangRotation = 0f
        override fun fixedDraw() {
            BottomTR.DrawOn(this)
            if (currentAmount > 0.001f) {
                LiquidBlock.drawTiledFrames(
                    size, x, y, liquidPadding,
                    currentFluid, currentAmount / liquidCapacity
                )
            }
            region.DrawOn(this)
            yinYangRotation = when (status) {
                P2pStatus.Sender -> Angles.moveToward(yinYangRotation, 0f, YinYangRotationSpeed * Time.delta)
                P2pStatus.Receiver -> Angles.moveToward(yinYangRotation, 180f, YinYangRotationSpeed * Time.delta)
                else -> Angles.moveToward(yinYangRotation, 0f, YinYangRotationSpeed * Time.delta)
            }
            YinAndYangTR.DrawOn(this, rotation = yinYangRotation)
        }

        override fun streamToAnother(amount: Float) {
            connected?.let {
                liquids.remove(currentFluid, amount)
                it.readSteam(currentFluid, amount)
            }
        }

        override fun readSteam(fluid: Liquid, amount: Float) {
            liquids.add(fluid, amount)
        }
        @ClientOnly
        @SendDataPack
        override fun onConfigureBuildTapped(other: Building): Boolean {
            if (this == other) {
                deselect()
                configure(null)
                return false
            }
            if (other == connected) {
                configure(null)
                return false
            }
            if (other is IP2pNode) {
                if (maxRange > 0f && other.dst(this) >= maxRange) {
                    postOverRangeOn(other)
                } else {
                    connected?.disconnectFromAnotherSync()
                    other.connected?.disconnectFromAnotherSync()
                    this.connectToSync(other)
                    other.connectToSync(this)
                    this.isDrawer = true
                    other.isDrawer = false
                }
                return false
            }
            return true
        }

        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            connectedPos = read.i()
        }

        override fun write(write: Writes) {
            super.write(write)
            write.i(connectedPos)
        }
    }

    @ClientOnly lateinit var NormalAni: AniStateP
    @ClientOnly lateinit var NoPowerAni: AniStateP
    override fun genAniState() {
        NormalAni = addAniState("Normal") {
        }
        NoPowerAni = addAniState("NoPower") {
            NoPowerTR.Draw(x, y)
        }
    }

    override fun genAniConfig() {
        config {
            From(NormalAni) To NoPowerAni When {
                !canConsume()
            }
            From(NoPowerAni) To NormalAni When {
                canConsume()
            }
        }
    }
}