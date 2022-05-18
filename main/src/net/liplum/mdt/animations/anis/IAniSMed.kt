package net.liplum.mdt.animations.anis

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
interface IAniSMed<TBlock : Block, TBuild : Building> {
    /**
     * Gets the Animation State by its name
     *
     * @param name [AniState.stateName]
     * @return if the name was registered, return the animation state. Otherwise, return null.
     */
    @ClientOnly
    fun getAniStateByName(name: String): AniState<TBlock, TBuild>?
    /**
     * Gets all Animation States.
     *
     * @return the collection of all Animation States
     */
    @ClientOnly
    val allAniStates: Collection<AniState<TBlock, TBuild>>
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
    var aniConfig: AniConfig<TBlock, TBuild>
    /**
     * Adds An Animation State
     *
     * @param aniState Animation State
     * @return `aniState` self
     */
    @ClientOnly
    fun addAniState(aniState: AniState<TBlock, TBuild>): AniState<TBlock, TBuild>
    /**
     * Creates a new Animation Config, and it will be returned.
     *
     * @return the Animation Config of this
     */
    @ClientOnly
    fun createAniConfig(): AniConfig<TBlock, TBuild>
    /**
     * @param name     name
     * @param renderer how to render
     * @return `aniState` self
     * For Kotlin
     */
    @ClientOnly
    fun addAniState(name: String, renderer: TBuild.() -> Unit): AniState<TBlock, TBuild> {
        return addAniState(AniState(name, renderer))
    }
    /**
     * @param name name
     * @return `aniState` self
     */
    @ClientOnly
    fun addAniState(name: String): AniState<TBlock, TBuild> {
        return addAniState(AniState(name))
    }
}