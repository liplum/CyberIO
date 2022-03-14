package net.liplum.api;

import mindustry.gen.Building;

public interface ITrigger<TBuild extends Building> {
    boolean canTrigger(TBuild build);
}
