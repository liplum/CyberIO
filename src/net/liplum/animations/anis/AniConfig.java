package net.liplum.animations.anis;

import arc.util.Nullable;
import mindustry.gen.Building;
import mindustry.world.Block;
import net.liplum.api.ITrigger;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * The configuration of an Animation State Machine
 *
 * @param <TBlock> the type of block which has this animation configuration
 * @param <TBuild> the corresponding {@link Building} type
 */
public class AniConfig<TBlock extends Block, TBuild extends Building> {
    /**
     * Key --to--> When can go to the next State
     */
    private final HashMap<Object, ITrigger<TBlock, TBuild>> canEnters = new HashMap<>();
    /**
     * Current State --to--> The next all possible State
     */
    private final HashMap<AniState<TBlock, TBuild>, List<AniState<TBlock, TBuild>>> allEntrances = new HashMap<>();
    /**
     * [NotNull,lateinit] The default and initial State.
     */
    private AniState<TBlock, TBuild> defaultState = null;
    /**
     * Whether this configuration has built
     */
    private boolean built = false;

    /**
     * Calculates the key of two ordered States
     *
     * @param from the current State
     * @param to   the next State
     * @return the key
     */
    private static Object getKey(@NotNull AniState<?, ?> from, @NotNull AniState<?, ?> to) {
        return from.hashCode() ^ (to.hashCode() * 2);
    }

    /**
     * Sets default State
     *
     * @param state the default State
     * @return this
     * @throws AlreadyBuiltException thrown when this configuration has already built
     */
    public AniConfig<TBlock, TBuild> defaultState(@NotNull AniState<TBlock, TBuild> state) {
        if (built) {
            throw new AlreadyBuiltException(this.toString());
        }
        this.defaultState = state;
        return this;
    }

    /**
     * Create an entry
     *
     * @param from     the current State
     * @param to       the next State
     * @param canEnter When the State Machine can go from current State to next State
     * @return this
     * @throws AlreadyBuiltException    thrown when this configuration has already built
     * @throws CannotEnterSelfException thrown when {@code from} equals to {@code to}
     */
    public AniConfig<TBlock, TBuild> entry(@NotNull AniState<TBlock, TBuild> from, @NotNull AniState<TBlock, TBuild> to,
                                           @NotNull ITrigger<TBlock, TBuild> canEnter) {
        if (built) {
            throw new AlreadyBuiltException(this.toString());
        }
        if (from == to || from.getStateName().equals(to.getStateName())) {
            throw new CannotEnterSelfException(this.toString());
        }
        Object key = getKey(from, to);
        canEnters.put(key, canEnter);
        List<AniState<TBlock, TBuild>> entrances = allEntrances.computeIfAbsent(from, k -> new LinkedList<>());
        entrances.add(to);
        return this;
    }

    /**
     * Builds the configuration. And this cannot be modified anymore.
     *
     * @return this
     * @throws NoDefaultStateException thrown when the default State hasn't been set yet
     */
    public AniConfig<TBlock, TBuild> build() {
        if (this.defaultState == null) {
            throw new NoDefaultStateException(this.toString());
        }
        this.built = true;
        return this;
    }

    /**
     * Generates the Animation State Machine object
     *
     * @param block the block of {@code build}
     * @param build which has the State Machine
     * @return an Animation State Machine
     * @throws HasNotBuiltYetException thrown when this hasn't built yet
     */
    public AniStateM<TBlock, TBuild> gen(TBlock block, TBuild build) {
        if (!built) {
            throw new HasNotBuiltYetException(this.toString());
        }
        return new AniStateM<>(this, block, build);
    }

    /**
     * Gets the condition for entering the {@code to} State from {@code from}
     *
     * @param from the current State
     * @param to   the next State
     * @return if the key of {@code form}->{@code to} exists, return the condition. Otherwise, return null.
     */
    @Nullable
    public ITrigger<TBlock, TBuild> getCanEnter(AniState<TBlock, TBuild> from, AniState<TBlock, TBuild> to) {
        return canEnters.get(getKey(from, to));
    }

    /**
     * Gets all possible States that {@code from} State can enter
     *
     * @param from the current State
     * @return a collection of States
     */
    public Collection<AniState<TBlock, TBuild>> getAllEntrances(AniState<TBlock, TBuild> from) {
        return allEntrances.computeIfAbsent(from, k -> new LinkedList<>());
    }

    /**
     * Gets the default State
     *
     * @return the default State
     */
    @Nullable
    public AniState<TBlock, TBuild> getDefaultState() {
        return defaultState;
    }

    public static class NoDefaultStateException extends RuntimeException {
        public NoDefaultStateException(String message) {
            super(message);
        }
    }

    public static class AlreadyBuiltException extends RuntimeException {
        public AlreadyBuiltException(String message) {
            super(message);
        }
    }

    public static class HasNotBuiltYetException extends RuntimeException {
        public HasNotBuiltYetException(String message) {
            super(message);
        }
    }

    public static class CannotEnterSelfException extends RuntimeException {
        public CannotEnterSelfException(String message) {
            super(message);
        }
    }


}
