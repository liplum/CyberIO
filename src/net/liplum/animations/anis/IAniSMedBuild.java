package net.liplum.animations.anis;

import mindustry.gen.Building;
import mindustry.world.Block;

public interface IAniSMedBuild<TBlock extends Block, TBuild extends Building> {
    AniStateM<TBlock, TBuild> getAniStateM();
}
