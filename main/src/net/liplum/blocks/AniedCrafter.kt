package net.liplum.blocks

import mindustry.Vars
import mindustry.world.blocks.production.GenericCrafter
import net.liplum.CanRefresh
import net.liplum.CioMod
import net.liplum.Meta
import net.liplum.Var
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.animations.anis.*
import net.liplum.utils.addAniStateInfo
import net.liplum.utils.addProgressInfo

@Suppress("UNCHECKED_CAST")
abstract class AniedCrafter<
        TBlock : AniedCrafter<TBlock, TBuild>,
        TBuild : AniedCrafter<TBlock, TBuild>.AniedCrafterBuild
        >(
    name: String
) :
    GenericCrafter(name), IAniSMed<TBlock, TBuild> {
    @ClientOnly
    override lateinit var aniConfig: AniConfig<TBlock, TBuild>
    @ClientOnly
    val name2AniStates = HashMap<String, AniState<TBlock, TBuild>>()
    @ClientOnly
    var callDefaultBlockDraw = true

    init {
        if (CioMod.IsClient) {
            genAniState()
            genAniConfig()
        }
    }

    override fun setBars() {
        super.setBars()
        if (Meta.EnableDebug) {
            addProgressInfo<GenericCrafterBuild>()
            addAniStateInfo<AniedCrafterBuild>()
        }
    }

    override fun createAniConfig(): AniConfig<TBlock, TBuild> {
        aniConfig = AniConfig()
        return aniConfig
    }

    override fun getAniStateByName(name: String): AniState<TBlock, TBuild>? {
        return name2AniStates[name]
    }

    override val allAniStates: Collection<AniState<TBlock, TBuild>>
        get() = name2AniStates.values

    override fun addAniState(aniState: AniState<TBlock, TBuild>): AniState<TBlock, TBuild> {
        name2AniStates[aniState.stateName] = aniState
        return aniState
    }

    abstract inner class AniedCrafterBuild : GenericCrafterBuild(), IAniSMedBuild<TBlock, TBuild> {
        override lateinit var aniStateM: AniStateM<TBlock, TBuild>

        init {
            if (CioMod.IsClient) {
                val out = this@AniedCrafter
                aniStateM = out.aniConfig.gen(out as TBlock, this as TBuild)
                aniStateM.onUpdate { onAniStateMUpdate() }
            }
        }
        @ClientOnly
        open fun onAniStateMUpdate() {
        }
        /**
         * Overwrite this please
         */
        @ClientOnly
        open fun beforeDraw() {
        }

        override fun draw() {
            if (!Vars.state.isPaused) {
                aniStateM.spend(delta())
                beforeDraw()
            }
            if (CanRefresh()) {
                aniStateM.update()
            }
            if (!aniStateM.curOverwriteBlock()) {
                super.draw()
            }
            aniStateM.drawBuilding()
        }
    }
}