package net.liplum.mdt.animation.state

inline fun <T> StateConfig<T>.configuring(
    config: StateConfiguringSpec<T>.() -> Unit,
): StateConfig<T> {
    val spec = StateConfiguringSpec(this).apply(config)
    val firstAdded = spec.firstAddedState
    if (firstAdded != null && this.defaultState == null) {
        defaultState = firstAdded
    }
    if (!this.built) this.build()
    return this
}

class StateConfiguringSpec<T>(
    val config: StateConfig<T>,
) {
    var firstAddedState: State<T>? = null
    fun State<T>.seDefaultState() {
        config.defaultState = this
    }

    inline operator fun State<T>.invoke(config: StateEntrySpec.() -> Unit) {
        if (firstAddedState == null) firstAddedState = this
        StateEntrySpec(this).apply(config)
    }

    inner class StateEntrySpec(val from: State<T>) {
        val setDefaultState: Unit
            get() = from.seDefaultState()

        operator fun State<T>.invoke(
            condition: T.() -> Boolean,
        ) {
            config.entry(from, this, condition)
        }
    }
}