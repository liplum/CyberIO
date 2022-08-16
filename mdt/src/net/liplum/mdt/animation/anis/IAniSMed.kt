package net.liplum.mdt.animation.anis

import mindustry.gen.Building
import mindustry.world.Block
import net.liplum.mdt.ClientOnly

/**
 * The interface of a block which has animation state machine
 *
 * @param <TBlock> the block type
 * @param <TBuild> its corresponding building type
 */
@ClientOnly
interface IAniSMed<TBuild : Building> {
    /**
     * Gets the Animation State by its name
     *
     * @param name [AniState.stateName]
     * @return if the name was registered, return the animation state. Otherwise, return null.
     */
    @ClientOnly
    fun getAniStateByName(name: String): AniState<TBuild>?
    /**
     * Gets all Animation States.
     *
     * @return the collection of all Animation States
     */
    @ClientOnly
    val allAniStates: Collection<AniState<TBuild>>
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
    var aniConfig: AniConfig<TBuild>
    /**
     * Adds An Animation State
     *
     * @param aniState Animation State
     * @return `aniState` self
     */
    @ClientOnly
    fun addAniState(aniState: AniState< TBuild>): AniState<TBuild>
    /**
     * Creates a new Animation Config, and it will be returned.
     *
     * @return the Animation Config of this
     */
    @ClientOnly
    fun createAniConfig(): AniConfig<TBuild>
    /**
     * @param name     name
     * @param renderer how to render
     * @return `aniState` self
     * For Kotlin
     */
    @ClientOnly
    fun addAniState(name: String, renderer: TBuild.() -> Unit): AniState<TBuild> {
        return addAniState(AniState(name, renderer))
    }
    /**
     * @param name name
     * @return `aniState` self
     */
    @ClientOnly
    fun addAniState(name: String): AniState<TBuild> {
        return addAniState(AniState(name))
    }
}

interface IAniSMedBuild< TBuild : Building> {
    val aniStateM: AniStateM<TBuild>
}