package net.liplum.animations.anis;

import arc.util.Nullable;
import mindustry.gen.Building;
import mindustry.world.Block;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class AniConfig<TBlock extends Block, TBuild extends Building> {
    private final HashMap<Object, ITrigger<TBlock, TBuild>> canEnters = new HashMap<>();
    private final HashMap<AniState<TBlock, TBuild>, List<AniState<TBlock, TBuild>>> allEntrances = new HashMap<>();
    private AniState<TBlock, TBuild> defaultState = null;
    private boolean built = false;

    private static Object getKey(AniState<?, ?> from, AniState<?, ?> to) {
        return from.hashCode() ^ (to.hashCode() * 2);
    }

    public AniConfig<TBlock, TBuild> defaultState(AniState<TBlock, TBuild> state) {
        if (built) {
            throw new AlreadyBuiltException(this.toString());
        }
        this.defaultState = state;
        return this;
    }

    public AniConfig<TBlock, TBuild> enter(AniState<TBlock, TBuild> from, AniState<TBlock, TBuild> to, ITrigger<TBlock, TBuild> canEnter) {
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

    public AniConfig<TBlock, TBuild> build() {
        if (this.defaultState == null) {
            throw new NoDefaultStateException(this.toString());
        }
        this.built = true;
        return this;
    }

    public AniStateM<TBlock, TBuild> gen(TBlock block, TBuild build) {
        if (!built) {
            throw new HasNotBuiltYetException(this.toString());
        }
        return new AniStateM<>(this, block, build);
    }

    @Nullable
    public ITrigger<TBlock, TBuild> getCanEnter(AniState<TBlock, TBuild> from, AniState<TBlock, TBuild> to) {
        return canEnters.get(getKey(from, to));
    }

    public List<AniState<TBlock, TBuild>> getAllEntrances(AniState<TBlock, TBuild> from) {
        return allEntrances.computeIfAbsent(from, k -> new LinkedList<>());
    }

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
