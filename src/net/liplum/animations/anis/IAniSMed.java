package net.liplum.animations.anis;

import arc.util.Nullable;
import mindustry.gen.Building;
import mindustry.world.Block;
import net.liplum.ClientOnly;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * The interface of a block which has animation state machine
 *
 * @param <TBlock> the block type
 * @param <TBuild> its corresponding building type
 */
@ClientOnly
public interface IAniSMed<TBlock extends Block, TBuild extends Building> {
    /**
     * Gets the Animation State by its name
     *
     * @param name {@link AniState#getStateName()}
     * @return if the name was registered, return the animation state. Otherwise, return null.
     */
    @Nullable
    @ClientOnly
    AniState<TBlock, TBuild> getAniStateByName(String name);

    /**
     * Gets building's Animation State Machine
     *
     * @param build building
     * @return Animation State Machine
     */
    @NotNull
    @ClientOnly
    AniStateM<TBlock, TBuild> getAniStateM(TBuild build);

    /**
     * Gets all Animation States.
     *
     * @return the collection of all Animation States
     */
    @ClientOnly
    Collection<AniState<TBlock, TBuild>> getAllAniStates();

    /**
     * Overwrite this.
     * Generates the Animation State.
     */
    @ClientOnly
    void genAniState();

    /**
     * Overwrite this.
     * Generates the configuration of the Animation State Machine
     */
    @ClientOnly
    void genAniConfig();

    /**
     * Gets the Animation State Machine Configuration of this
     *
     * @return Animation State Configuration
     */
    @ClientOnly
    AniConfig<TBlock, TBuild> getAniConfig();

    /**
     * Sets the Animation State Machine Configuration of this
     *
     * @param config Animation State Configuration
     */
    @ClientOnly
    void setAniConfig(AniConfig<TBlock, TBuild> config);

    /**
     * Adds An Animation State
     *
     * @param aniState Animation State
     * @return {@code aniState} self
     */
    @ClientOnly
    AniState<TBlock, TBuild> addAniState(AniState<TBlock, TBuild> aniState);

    /**
     * Creates a new Animation Config, and it will be returned.
     *
     * @return the Animation Config of this
     */
    @ClientOnly
    AniConfig<TBlock, TBuild> createAniConfig();

    /**
     * @param name name
     * @param rb   how to render
     * @return {@code aniState} self
     */
    @ClientOnly
    default AniState<TBlock, TBuild> addAniState(String name, IRenderBehavior<TBuild> rb) {
        return addAniState(new AniState<>(name, rb));
    }

    /**
     * @param name name
     * @return {@code aniState} self
     */
    @ClientOnly
    default AniState<TBlock, TBuild> addAniState(String name) {
        return addAniState(new AniState<>(name));
    }
}
