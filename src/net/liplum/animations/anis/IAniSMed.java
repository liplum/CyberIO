package net.liplum.animations.anis;

import arc.util.Nullable;
import mindustry.gen.Building;
import mindustry.world.Block;

import java.util.Collection;

public interface IAniSMed<TBlock extends Block, TBuild extends Building> {
    @Nullable
    AniState<TBlock, TBuild> getAniStateByName(String name);

    Collection<AniState<TBlock, TBuild>> getAllAniStates();

    void genAniState();

    void genAniConfig();

    AniConfig<TBlock, TBuild> getAniConfig();

    void setAniConfig(AniConfig<TBlock, TBuild> config);

    AniState<TBlock, TBuild> addAniState(AniState<TBlock, TBuild> aniState);

    default AniState<TBlock, TBuild> addAniState(String name, IRenderBehavior<TBlock, TBuild> rb) {
        return addAniState(new AniState<>(name, rb));
    }
}
