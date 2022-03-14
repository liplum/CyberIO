package net.liplum.animations.anis;

import mindustry.gen.Building;

public interface IRenderBehavior<TBuild extends Building> {
    /**
     * How to render the {@code building}
     *
     * @param build the building to be rendered
     */
    void drawBuild(TBuild build);
}