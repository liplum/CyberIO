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

inline fun <T> StateConfig<T>.configuring(
    config: StateConfiguringSpec<T>.() -> Unit,
): StateConfig<T> {
    StateConfiguringSpec(this).apply(config)
    if (!this.built) this.build()
    return this
}

class StateConfiguringSpec<T>(
    val config: StateConfig<T>,
) {
    fun State<T>.asDefaultState() {
        config.defaultState(this)
    }

    inline operator fun State<T>.invoke(config: StateEntrySpec.() -> Unit) {
        StateEntrySpec(this).apply(config)
    }

    inner class StateEntrySpec(val from: State<T>) {
        operator fun State<T>.invoke(
            condition: T.() -> Boolean,
        ) {
            config.entry(from, this, condition)
        }
    }
}