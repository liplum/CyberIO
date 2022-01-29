package net.liplum.animations;

import arc.util.Nullable;
import mindustry.gen.Building;
import mindustry.world.Block;

import java.util.Collection;

public interface IAniSMed<TBlock extends Block, TBuild extends Building> {
    @Nullable
    AniState<TBlock, TBuild> getAniStateByName(String name);

    Collection<AniState<TBlock, TBuild>> getAllAniStates();

    void genAnimState();

    void genAniConfig();

    AniConfig<TBlock, TBuild> getAniConfig();

    void setAniConfig(AniConfig<TBlock, TBuild> config);
}
