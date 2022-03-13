package net.liplum.api;

import mindustry.gen.Building;
import mindustry.world.Block;

public interface ITrigger<TBuild extends Building> {
    boolean canTrigger(TBuild build);
}
