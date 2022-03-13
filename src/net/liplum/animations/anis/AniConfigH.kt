package net.liplum.animations.anis

import mindustry.gen.Building
import mindustry.world.Block

inline fun <TBlock, TBuild> IAniSMed<TBlock, TBuild>.config(config: AniConfig<TBlock, TBuild>.() -> Unit)
        : AniConfig<TBlock, TBuild>
        where TBlock : Block, TBuild : Building {
    val aniConfig = this.createAniConfig()
    aniConfig.config()
    if (!aniConfig.built) {
        if (aniConfig.defaultState == null) {
            val firstConfigedState = aniConfig.firstConfigedState
            if (firstConfigedState != null) {
                aniConfig.defaultState(firstConfigedState)
            } else {
                aniConfig.defaultState(aniConfig.allConfigedStates.first())
            }
        }
        aniConfig.build()
    }
    return aniConfig
}

inline fun <TBlock, TBuild> config(config: AniConfig<TBlock, TBuild>.() -> Unit)
        : AniConfig<TBlock, TBuild>
        where TBlock : Block, TBuild : Building {
    val aniConfig = AniConfig<TBlock, TBuild>()
    aniConfig.config()
    if (!aniConfig.built) {
        if (aniConfig.defaultState == null) {
            val firstConfigedState = aniConfig.firstConfigedState
            if (firstConfigedState != null) {
                aniConfig.defaultState(firstConfigedState)
            } else {
                aniConfig.defaultState(aniConfig.allConfigedStates.first())
            }
        }
        aniConfig.build()
    }
    return aniConfig
}
