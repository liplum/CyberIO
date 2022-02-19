package net.liplum.animations.anis;

import mindustry.gen.Building;
import mindustry.world.Block;

public interface ISwitchAniStateListener<TBlock extends Block, TBuild extends Building> {
    void onSwitch(TBlock block, TBuild build, AniState<TBlock, TBuild> from, AniState<TBlock, TBuild> to);
}
