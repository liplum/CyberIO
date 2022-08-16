package net.liplum.mdt.animation.state

import mindustry.gen.Building
import net.liplum.mdt.ClientOnly

/**
 * The interface of a block which has animation state machine
 *
 * @param <TBlock> the block type
 * @param <TBuild> its corresponding building type
 */
@ClientOnly
interface IStateMachined<TBuild : Building> {
    /**
     * Gets the Animation State by its name
     *
     * @param name [State.stateName]
     * @return if the name was registered, return the animation state. Otherwise, return null.
     */
    @ClientOnly
    fun getAniStateByName(name: String): State<TBuild>?
    /**
     * Gets all Animation States.
     *
     * @return the collection of all Animation States
     */
    @ClientOnly
    val allAniStates: Collection<State<TBuild>>
    /**
     * Overwrite this.
     * Generates the Animation State.
     */
    @ClientOnly
    fun genAniState()
    /**
     * Overwrite this.
     * Generates the configuration of the Animation State Machine
     */
    @ClientOnly
    fun genAniConfig()
    /**
     * the Animation State Machine Configuration of this
     */
    @ClientOnly
    var aniConfig: StateConfig<TBuild>
    /**
     * Adds An Animation State
     *
     * @param aniState Animation State
     * @return `aniState` self
     */
    @ClientOnly
    fun addAniState(aniState: State< TBuild>): State<TBuild>
    /**
     * Creates a new Animation Config, and it will be returned.
     *
     * @return the Animation Config of this
     */
    @ClientOnly
    fun createAniConfig(): StateConfig<TBuild>
    /**
     * @param name     name
     * @param renderer how to render
     * @return `aniState` self
     * For Kotlin
     */
    @ClientOnly
    fun addAniState(name: String, renderer: TBuild.() -> Unit): State<TBuild> {
        return addAniState(State(name, renderer))
    }
    /**
     * @param name name
     * @return `aniState` self
     */
    @ClientOnly
    fun addAniState(name: String): State<TBuild> {
        return addAniState(State(name))
    }
}

interface IAniSMedBuild< TBuild : Building> {
    val aniStateM: StateMachine<TBuild>
}