package net.liplum.blocks.stream

import arc.func.Prov
import mindustry.gen.Building
import mindustry.type.Liquid
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
import net.liplum.mdt.render.Text
import net.liplum.mdt.utils.PackedPos
import net.liplum.mdt.utils.inMod

private typealias AniStateP = AniState<P2pNode, P2pNode.P2pBuild>

open class P2pNode(name: String) : AniedBlock<P2pNode, P2pNode.P2pBuild>(name) {
    @ClientOnly lateinit var NoPowerTR: TR
    @JvmField var balancingSpeed = 1f
    /**
     * The max range when trying to connect. -1f means no limit.
     */
    @JvmField var maxRange = -1f
    @ClientOnly @JvmField var maxSelectedCircleTime = Var.SelectedCircleTime

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
        config(java.lang.Integer::class.java) { b: P2pBuild, i ->
            b.setConnectedPosFromRemote(i.toInt())
        }
        configClear<P2pBuild> {
            it.setConnectedPosFromRemote(-1)
        }
    }

    override fun load() {
        super.load()
        NoPowerTR = this.inMod("rs-no-power")
    }

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
        var connectedPos: PackedPos = -1
        override var connected: IP2pNode?
            get() = connectedPos.p2p()
            @SendDataPack(["setConnectedPosFromRemote(PackedPos)", "connectedPos::set"])
            set(value) {
                configure(value?.building?.pos() ?: -1)
            }

        override fun acceptLiquid(source: Building, liquid: Liquid): Boolean {
            val connected = connected
            return if (connected != null)
                connected.currentFluid == liquid && liquids[liquid] < liquidCapacity
            else
                (currentFluid == liquid || currentAmount < 0.2f) && liquids[liquid] < liquidCapacity
        }
        @CalledBySync
        fun setConnectedPosFromRemote(pos: PackedPos) {
            connectedPos = pos
        }

        override fun updateTile() {
            connected?.let {
                if (it.connected != this)
                    it.connected = this
            }
            if (currentAmount > 0.001f) {
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
                    other.connected = this
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