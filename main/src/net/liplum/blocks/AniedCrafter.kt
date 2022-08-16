package net.liplum.blocks

import mindustry.world.blocks.production.GenericCrafter
import net.liplum.CanRefresh
import net.liplum.DebugOnly
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.WhenNotPaused
import net.liplum.mdt.animation.state.*
import net.liplum.util.addAniStateInfo
import net.liplum.util.addProgressInfo

@Suppress("UNCHECKED_CAST")
abstract class AniedCrafter<
        TBuild : AniedCrafter<TBuild>.AniedCrafterBuild,
        >(
    name: String,
) :
    GenericCrafter(name), IStateMachined<TBuild> {
    @ClientOnly
    override lateinit var aniConfig: StateConfig<TBuild>
    @ClientOnly
    val name2AniStates = HashMap<String, State<TBuild>>()
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

    override fun createAniConfig(): StateConfig<TBuild> {
        aniConfig = StateConfig()
        return aniConfig
    }

    override fun getAniStateByName(name: String): State<TBuild>? {
        return name2AniStates[name]
    }

    override val allAniStates: Collection<State<TBuild>>
        get() = name2AniStates.values

    override fun addAniState(aniState: State<TBuild>): State<TBuild> {
        name2AniStates[aniState.stateName] = aniState
        return aniState
    }

    abstract inner class AniedCrafterBuild : GenericCrafterBuild(), IAniSMedBuild<TBuild> {
        override lateinit var aniStateM: StateMachine<TBuild>

        init {
            ClientOnly {
                val out = this@AniedCrafter
                aniStateM = out.aniConfig.instantiate(this as TBuild)
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
                aniStateM.updateState()
            }
            if (!aniStateM.curOverwriteBlock()) {
                super.draw()
            }
            aniStateM.drawBuilding()
        }
    }
}