package net.liplum.mdt.animation.state

import mindustry.gen.Building

inline fun <TBuild> IStateMachined<TBuild>.configStateMachine(config: StateConfig<TBuild>.() -> Unit)
        : StateConfig<TBuild>
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

inline fun <TBuild> configStateMachine(config: StateConfig<TBuild>.() -> Unit)
        : StateConfig<TBuild>
        where TBuild : Building {
    val aniConfig = StateConfig<TBuild>()
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
