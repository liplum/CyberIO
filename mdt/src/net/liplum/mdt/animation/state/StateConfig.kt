package net.liplum.mdt.animation.state

import arc.util.Nullable
import mindustry.Vars
import mindustry.gen.Building
import java.util.*

typealias TransitionEffect = (Float, () -> Unit, () -> Unit) -> Unit

/**
 * The configuration of an Animation State Machine
 */
open class StateConfig<T> {
    /**
     * Key --to--> When can go to the next State
     */
    private val canEnters = HashMap<Any, T.() -> Boolean>()
    /**
     * Current State --to--> The next all possible State
     */
    private val allEntrances = HashMap<State<T>, MutableList<State<T>>>()
    var firstConfigedState: State<T>? = null
    /**
     * Gets the default State
     *
     * @return the default State
     */
    /**
     * [NotNull,lateinit] The default and initial State.
     */
    var defaultState: State<T>? = null
        private set
    /**
     * Whether this configuration has built
     */
    var built = false
        private set
    var transition: TransitionEffect = SmoothFade
        set(value) {
            checkBuilt()
            field = value
        }
    var transitionDuration: Float = 30f
        set(value) {
            checkBuilt()
            field = value
        }
    /**
     * Which from State is configuring
     */
    private var curConfiguringFromState: State<T>? = null
    /**
     * Which to State is configuring
     */
    private var curConfiguringToState: State<T>? = null
    /**
     * Sets default State
     *
     * @param state the default State
     * @return this
     * @throws IllegalStateException thrown when this configuration has already built
     */
    open fun defaultState(state: State<T>): StateConfig<T> {
        checkBuilt()
        defaultState = state
        return this
    }
    /**
     * Sets the transition effect
     */
    open fun transition(transitionEffect: TransitionEffect): StateConfig<T> {
        checkBuilt()
        transition = transitionEffect
        return this
    }

    open fun transitionDuration(duration: Float): StateConfig<T> {
        checkBuilt()
        transitionDuration = duration
        return this
    }
    /**
     * Check whether this Animation Config has been built.
     * @throws IllegalStateException thrown when this configuration has already built
     */
    private fun checkBuilt() {
        if (built) {
            throw IllegalStateException("$this has already built.")
        }
    }

    val allConfigedStates: MutableSet<State<T>>
        get() = allEntrances.keys
    /**
     * Creates an entry
     *
     * @param from     the current State
     * @param to       the next State
     * @param canEnter When the State Machine can go from current State to next State
     * @return this
     * @throws IllegalStateException    thrown when this configuration has already built
     * @throws CannotEnterSelfException thrown when `from` equals to `to`
     */
    open fun entry(
        from: State<T>, to: State<T>,
        canEnter: T.() -> Boolean,
    ): StateConfig<T> {
        checkBuilt()
        if (from == to || from.stateName == to.stateName) {
            throw CannotEnterSelfException(this.toString())
        }
        curConfiguringFromState = from
        val key = getKey(from, to)
        canEnters[key] = canEnter
        val entrances = allEntrances.getOrPut(from) { LinkedList() }
        entrances.add(to)
        return this
    }
    /**
     * Sets the "from" State
     *
     * @param from the current State
     * @return this
     */
    open infix fun From(from: State<T>): StateConfig<T> {
        checkBuilt()
        if (firstConfigedState == null) {
            firstConfigedState = from
        }
        curConfiguringFromState = from
        return this
    }
    /**
     * Creates an entry with "from" State
     *
     * @param to       the next State
     * @param canEnter When the State Machine can go from current State to next State
     * @return this
     */
    open fun To(to: State<T>, canEnter: T.() -> Boolean): StateConfig<T> {
        checkBuilt()
        if (curConfiguringFromState == null) {
            throw NoFromStateException(this.toString())
        }
        val ccf = curConfiguringFromState!!
        if (ccf == to || ccf.stateName == to.stateName) {
            throw CannotEnterSelfException(this.toString())
        }
        val key = getKey(ccf, to)
        canEnters[key] = canEnter
        val entrances = allEntrances.getOrPut(ccf) { LinkedList() }
        entrances.add(to)
        return this
    }
    /**
     * Sets the "to" State
     *
     * @param to the next State
     * @return this
     */
    open infix fun To(to: State<T>): StateConfig<T> {
        checkBuilt()
        curConfiguringToState = to
        return this
    }
    /**
     * Creates an entry with "from" and "to" States
     *
     * @param canEnter When the State Machine can go from current State to next State
     * @return this
     */
    open infix fun When(canEnter: T.() -> Boolean): StateConfig<T> {
        checkBuilt()
        if (curConfiguringFromState == null) {
            throw NoFromStateException(this.toString())
        }
        if (curConfiguringToState == null) {
            throw NoToStateException(this.toString())
        }
        if (curConfiguringFromState == curConfiguringToState || curConfiguringFromState!!.stateName == curConfiguringToState!!.stateName) {
            throw CannotEnterSelfException(this.toString())
        }
        val key = getKey(
            curConfiguringFromState!!,
            curConfiguringToState!!
        )
        canEnters[key] = canEnter
        val entrances = allEntrances.getOrPut(curConfiguringFromState!!) { LinkedList() }
        entrances.add(curConfiguringToState!!)
        return this
    }
    /**
     * Builds the configuration. And this cannot be modified anymore.
     *
     * @return this
     * @throws NoDefaultStateException thrown when the default State hasn't been set yet
     */
    open fun build(): StateConfig<T> {
        if (defaultState == null) {
            throw NoDefaultStateException(this.toString())
        }
        built = true
        return this
    }
    /**
     * Instantiate the [StateMachine]
     *
     * @param build which has the State Machine
     * @return an Animation State Machine
     * @throws IllegalStateException thrown when this hasn't built yet
     */
    open fun instantiate(build: T): StateMachine<T> {
        if (!built) {
            throw IllegalStateException("$this has not built.")
        }
        return StateMachine(this, build)
    }
    /**
     * Gets the condition for entering the `to` State from `from`
     *
     * @param from the current State
     * @param to   the next State
     * @return if the key of `form`->`to` exists, return the condition. Otherwise, return null.
     */
    @Nullable
    open fun getCanEnter(from: State<T>, to: State<T>): (T.() -> Boolean)? {
        return canEnters[getKey(from, to)]
    }
    /**
     * Gets all possible States that `from` State can enter
     *
     * @param from the current State
     * @return a collection of States
     */
    open fun getAllEntrances(from: State<T>): Collection<State<T>> {
        return allEntrances.getOrPut(from) { LinkedList() }
    }

    class NoDefaultStateException(message: String) : RuntimeException(message)
    class CannotEnterSelfException(message: String) : RuntimeException(message)
    class NoFromStateException(message: String) : RuntimeException(message)
    class NoToStateException(message: String) : RuntimeException(message)
    companion object {
        /**
         * Calculates the key of two ordered States
         *
         * @param from the current State
         * @param to   the next State
         * @return the key
         */
        @JvmStatic
        private fun getKey(from: State<*>, to: State<*>): Any {
            return from.hashCode() xor to.hashCode() * 2
        }
    }
}