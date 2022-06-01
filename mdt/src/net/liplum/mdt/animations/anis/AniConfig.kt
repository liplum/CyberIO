package net.liplum.mdt.animations.anis

import arc.util.Nullable
import mindustry.gen.Building
import mindustry.world.Block
import java.util.*

typealias TransitionEffect = (Float, () -> Unit, () -> Unit) -> Unit

/**
 * The configuration of an Animation State Machine
 *
 * @param <TBlock> the type of block which has this animation configuration
 * @param <TBuild> the corresponding [Building] type
</TBuild></TBlock> */
open class AniConfig<TBlock : Block, TBuild : Building> {
    /**
     * Key --to--> When can go to the next State
     */
    private val canEnters = HashMap<Any, TBuild.() -> Boolean>()
    /**
     * Current State --to--> The next all possible State
     */
    private val allEntrances = HashMap<AniState<TBlock, TBuild>, MutableList<AniState<TBlock, TBuild>>>()
    var firstConfigedState: AniState<TBlock, TBuild>? = null
    /**
     * Gets the default State
     *
     * @return the default State
     */
    /**
     * [NotNull,lateinit] The default and initial State.
     */
    var defaultState: AniState<TBlock, TBuild>? = null
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
    private var curConfiguringFromState: AniState<TBlock, TBuild>? = null
    /**
     * Which to State is configuring
     */
    private var curConfiguringToState: AniState<TBlock, TBuild>? = null
    /**
     * Sets default State
     *
     * @param state the default State
     * @return this
     * @throws AlreadyBuiltException thrown when this configuration has already built
     */
    open fun defaultState(state: AniState<TBlock, TBuild>): AniConfig<TBlock, TBuild> {
        checkBuilt()
        defaultState = state
        return this
    }
    /**
     * Sets the transition effect
     */
    open fun transition(transitionEffect: TransitionEffect): AniConfig<TBlock, TBuild> {
        checkBuilt()
        transition = transitionEffect
        return this
    }

    open fun transitionDuration(duration: Float): AniConfig<TBlock, TBuild> {
        checkBuilt()
        transitionDuration = duration
        return this
    }
    /**
     * Check whether this Animation Config has been built.
     * Raising [AlreadyBuiltException] when built
     * @throws AlreadyBuiltException thrown when this configuration has already built
     */
    private fun checkBuilt() {
        if (built) {
            throw AlreadyBuiltException(this.toString())
        }
    }

    val allConfigedStates: MutableSet<AniState<TBlock, TBuild>>
        get() = allEntrances.keys
    /**
     * Creates an entry
     *
     * @param from     the current State
     * @param to       the next State
     * @param canEnter When the State Machine can go from current State to next State
     * @return this
     * @throws AlreadyBuiltException    thrown when this configuration has already built
     * @throws CannotEnterSelfException thrown when `from` equals to `to`
     */
    open fun entry(
        from: AniState<TBlock, TBuild>, to: AniState<TBlock, TBuild>,
        canEnter: TBuild.() -> Boolean,
    ): AniConfig<TBlock, TBuild> {
        checkBuilt()
        if (from === to || from.stateName == to.stateName) {
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
    open infix fun From(from: AniState<TBlock, TBuild>): AniConfig<TBlock, TBuild> {
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
    open fun To(to: AniState<TBlock, TBuild>, canEnter: TBuild.() -> Boolean): AniConfig<TBlock, TBuild> {
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
    open infix fun To(to: AniState<TBlock, TBuild>): AniConfig<TBlock, TBuild> {
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
    open infix fun When(canEnter: TBuild.() -> Boolean): AniConfig<TBlock, TBuild> {
        checkBuilt()
        if (curConfiguringFromState == null) {
            throw NoFromStateException(this.toString())
        }
        if (curConfiguringToState == null) {
            throw NoToStateException(this.toString())
        }
        if (curConfiguringFromState === curConfiguringToState || curConfiguringFromState!!.stateName == curConfiguringToState!!.stateName) {
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
    open fun build(): AniConfig<TBlock, TBuild> {
        if (defaultState == null) {
            throw NoDefaultStateException(this.toString())
        }
        built = true
        return this
    }
    /**
     * Generates the Animation State Machine object
     *
     * @param block the block of `build`
     * @param build which has the State Machine
     * @return an Animation State Machine
     * @throws HasNotBuiltYetException thrown when this hasn't built yet
     */
    open fun gen(block: TBlock, build: TBuild): AniStateM<TBlock, TBuild> {
        if (!built) {
            throw HasNotBuiltYetException(this.toString())
        }
        return AniStateM(this, block, build)
    }
    /**
     * Gets the condition for entering the `to` State from `from`
     *
     * @param from the current State
     * @param to   the next State
     * @return if the key of `form`->`to` exists, return the condition. Otherwise, return null.
     */
    @Nullable
    open fun getCanEnter(from: AniState<TBlock, TBuild>, to: AniState<TBlock, TBuild>): (TBuild.() -> Boolean)? {
        return canEnters[getKey(from, to)]
    }
    /**
     * Gets all possible States that `from` State can enter
     *
     * @param from the current State
     * @return a collection of States
     */
    open fun getAllEntrances(from: AniState<TBlock, TBuild>): Collection<AniState<TBlock, TBuild>> {
        return allEntrances.getOrPut(from) { LinkedList() }
    }

    class NoDefaultStateException(message: String) : RuntimeException(message)
    class AlreadyBuiltException(message: String) : RuntimeException(message)
    class HasNotBuiltYetException(message: String) : RuntimeException(message)
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
        private fun getKey(from: AniState<*, *>, to: AniState<*, *>): Any {
            return from.hashCode() xor to.hashCode() * 2
        }
    }
}