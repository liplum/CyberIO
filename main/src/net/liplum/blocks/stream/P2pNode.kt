package net.liplum.blocks.stream

import arc.func.Prov
import mindustry.gen.Building
import mindustry.type.Liquid
import mindustry.world.blocks.liquid.LiquidBlock
import mindustry.world.meta.BlockGroup
import net.liplum.R
import net.liplum.Var
import net.liplum.api.cyber.*
import net.liplum.blocks.AniedBlock
import net.liplum.lib.Serialized
import net.liplum.lib.assets.TR
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
    @ClientOnly lateinit var NoPowerTR: TR
    @JvmField var balancingSpeed = 1f
    /**
     * The max range when trying to connect. -1f means no limit.
     */
    @JvmField var maxRange = -1f
    @ClientOnly @JvmField var maxSelectedCircleTime = Var.SelectedCircleTime
    @ClientOnly lateinit var TopTR: TR

    init {
        buildType = Prov { P2pBuild() }
        hasLiquids = true
        update = true
        solid = true
        group = BlockGroup.liquids
        outputsLiquid = true
        configurable = true
        noUpdateDisabled = true
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
        TopTR = this.sub("top")
    }

    override fun icons() = arrayOf(region, TopTR)
    override fun setBars() {
        super.setBars()
    }

    open inner class P2pBuild : AniedBuild(), IP2pNode {
        override val maxRange = this@P2pNode.maxRange
        override val currentFluid: Liquid
            get() = liquids.current()
        override val currentAmount: Float
            get() = liquids.currentAmount()
        @Serialized
        @set:CalledBySync
        override var connectedPos: PackedPos = -1
        @SendDataPack(["setConnectedPosFromRemote(PackedPos)", "connectedPos::set"])
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
        @CalledBySync
        fun setConnectedPosFromRemote(pos: PackedPos) {
            if (pos == -1) {
                this.connectedPos = -1
            } else {
                this.connectedPos = pos
            }
        }

        override fun updateTile() {
            val other = connected
            run {
                if (other != null) {
                    if(this.currentFluid != other.currentFluid && other.currentAmount > 0.02f) return@run
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
            if (currentAmount > 0.0001f) {
                dumpLiquid(currentFluid)
            }
        }

        override fun draw() {
            super.draw()
            connected?.let {
                Text.drawTextEasy("(${it.tileX},${it.tileY})", x, y + 10f)
                transferArrowLineBreath(
                    this, it.building,
                    arrowColor = R.C.Holo,
                    density = ArrowDensity,
                    speed = ArrowSpeed,
                    alpha = 0.8f
                )
            }
        }

        override fun fixedDraw() {
            LiquidBlock.drawTiledFrames(
                size, x, y, 0f,
                currentFluid, currentAmount / liquidCapacity
            )
            TopTR.DrawOn(this)
        }

        fun balance() {
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
                }
                return false
            }
            return true
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