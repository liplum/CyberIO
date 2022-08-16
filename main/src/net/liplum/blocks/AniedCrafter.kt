package net.liplum.blocks

import mindustry.world.blocks.production.GenericCrafter
import net.liplum.CanRefresh
import net.liplum.DebugOnly
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.WhenNotPaused
import net.liplum.mdt.animation.anis.*
import net.liplum.util.addAniStateInfo
import net.liplum.util.addProgressInfo

@Suppress("UNCHECKED_CAST")
abstract class AniedCrafter<
        TBuild : AniedCrafter<TBuild>.AniedCrafterBuild,
        >(
    name: String,
) :
    GenericCrafter(name), IAniSMed<TBuild> {
    @ClientOnly
    override lateinit var aniConfig: AniConfig<TBuild>
    @ClientOnly
    val name2AniStates = HashMap<String, AniState<TBuild>>()
    @ClientOnly
    var callDefaultBlockDraw = true

    init {
        ClientOnly {
            genAniState()
            genAniConfig()
        }
    }

    override fun setBars() {
        super.setBars()
        DebugOnly {
            addProgressInfo<GenericCrafterBuild>()
            addAniStateInfo<AniedCrafterBuild>()
        }
    }

    override fun createAniConfig(): AniConfig<TBuild> {
        aniConfig = AniConfig()
        return aniConfig
    }

    override fun getAniStateByName(name: String): AniState<TBuild>? {
        return name2AniStates[name]
    }

    override val allAniStates: Collection<AniState<TBuild>>
        get() = name2AniStates.values

    override fun addAniState(aniState: AniState<TBuild>): AniState<TBuild> {
        name2AniStates[aniState.stateName] = aniState
        return aniState
    }

    abstract inner class AniedCrafterBuild : GenericCrafterBuild(), IAniSMedBuild<TBuild> {
        override lateinit var aniStateM: AniStateM<TBuild>

        init {
            ClientOnly {
                val out = this@AniedCrafter
                aniStateM = out.aniConfig.gen(this as TBuild)
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
            WhenNotPaused {
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