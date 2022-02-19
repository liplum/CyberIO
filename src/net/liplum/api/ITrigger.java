package net.liplum.api;

import mindustry.gen.Building;
import mindustry.world.Block;

public interface ITrigger<TBlock extends Block, TBuild extends Building> {
    boolean canTrigger(TBlock block, TBuild build);
}
