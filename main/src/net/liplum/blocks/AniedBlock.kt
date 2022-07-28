package net.liplum.blocks

import arc.util.Nullable
import mindustry.gen.Building
import mindustry.world.Block
import net.liplum.CanRefresh
import net.liplum.Meta
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.WhenNotPaused
import net.liplum.mdt.animation.anis.*
import net.liplum.util.addAniStateInfo

/**
 * A block which has animation state machine
 *
 * @param <TBlock> the block type
 * @param <TBuild> its corresponding building type
 */
@Suppress("UNCHECKED_CAST")
abstract class AniedBlock<
        TBlock : AniedBlock<TBlock, TBuild>,
        TBuild : AniedBlock<TBlock, TBuild>.AniedBuild,
        >(
    name: String,
) : Block(name), IAniSMed<TBlock, TBuild> {
    @ClientOnly
    protected val name2AniStates = HashMap<String, AniState<TBlock, TBuild>>()
    @ClientOnly
    var callDefaultBlockDraw = true
    @ClientOnly
    override lateinit var aniConfig: AniConfig<TBlock, TBuild>

    init {
        ClientOnly {
            genAniState()
            genAniConfig()
        }
    }

    override fun setBars() {
        super.setBars()
        if (Meta.EnableDebug) {
            addAniStateInfo<AniedBuild>()
        }
    }
    @Nullable
    override fun getAniStateByName(name: String): AniState<TBlock, TBuild>? {
        return name2AniStates[name]
    }

    override val allAniStates: Collection<AniState<TBlock, TBuild>>
        get() = name2AniStates.values

    override fun addAniState(aniState: AniState<TBlock, TBuild>): AniState<TBlock, TBuild> {
        name2AniStates[aniState.stateName] = aniState
        return aniState
    }

    override fun createAniConfig(): AniConfig<TBlock, TBuild> {
        aniConfig = AniConfig()
        return aniConfig
    }
    /**
     * You have to make the [Building] of your subclass extend this
     */
    abstract inner class AniedBuild : Building(), IAniSMedBuild<TBlock, TBuild> {
        override lateinit var aniStateM: AniStateM<TBlock, TBuild>

        init {
            ClientOnly {
                val outer = this@AniedBlock
                aniStateM = outer.aniConfig.gen(outer as TBlock, this as TBuild)
            }
        }
        /**
         * Overwrite this please
         */
        @ClientOnly
        open fun fixedDraw() {
        }
        /**
         * Overwrite this please
         */
        @ClientOnly
        open fun beforeDraw() {
        }
        /**
         * Don't overwrite it unless you want a custom function
         */
        override fun draw() {
            WhenNotPaused {
                aniStateM.spend(delta())
                beforeDraw()
            }
            if (CanRefresh()) {
                aniStateM.update()
            }
            if (callDefaultBlockDraw && !aniStateM.curOverwriteBlock()) {
                super.draw()
            }
            fixedDraw()
            aniStateM.drawBuilding()
        }
    }
}