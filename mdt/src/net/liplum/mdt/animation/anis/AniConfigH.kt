package net.liplum.mdt.animation.anis

import mindustry.gen.Building

inline fun <TBuild> IAniSMed<TBuild>.configStates(config: AniConfig<TBuild>.() -> Unit)
        : AniConfig<TBuild>
        where TBuild : Building {
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

inline fun <TBuild> configStates(config: AniConfig<TBuild>.() -> Unit)
        : AniConfig<TBuild>
        where TBuild : Building {
    val aniConfig = AniConfig<TBuild>()
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
