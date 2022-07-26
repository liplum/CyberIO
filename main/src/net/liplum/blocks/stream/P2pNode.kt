package net.liplum.blocks.stream

import arc.func.Prov
import mindustry.gen.Building
import mindustry.type.Liquid
import mindustry.world.meta.BlockGroup
import net.liplum.api.cyber.IP2pNode
import net.liplum.api.cyber.p2p
import net.liplum.blocks.AniedBlock
import net.liplum.lib.Serialized
import net.liplum.lib.assets.TR
import net.liplum.mdt.CalledBySync
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.SendDataPack
import net.liplum.mdt.animations.anis.AniState
import net.liplum.mdt.animations.anis.config
import net.liplum.mdt.render.Draw
import net.liplum.mdt.utils.PackedPos
import net.liplum.mdt.utils.inMod

private typealias AniStateP = AniState<P2pNode, P2pNode.P2pBuild>

open class P2pNode(name: String) : AniedBlock<P2pNode, P2pNode.P2pBuild>(name) {
    @ClientOnly lateinit var NoPowerTR: TR
    @JvmField var balancingSpeed = 1f

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
    }

    override fun load() {
        super.load()
        NoPowerTR = this.inMod("rs-no-power")
    }

    open inner class P2pBuild : AniedBuild(), IP2pNode {
        override val currentFluid: Liquid
            get() = liquids.current()
        override val currentAmount: Float
            get() = liquids.currentAmount()
        @Serialized
        @set:CalledBySync
        @JvmField
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
            if (currentAmount > 0.001f) {
                dumpLiquid(currentFluid)
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