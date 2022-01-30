package net.liplum.animations.anis;

import mindustry.gen.Building;
import mindustry.world.Block;

public interface ISwitchAniStateListener<TBlock extends Block, TBuild extends Building> {
    void OnSwitch(TBlock block, TBuild build, AniState<TBlock, TBuild> originalState, AniState<TBlock, TBuild> newState);
}
