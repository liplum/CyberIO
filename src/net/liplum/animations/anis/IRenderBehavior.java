package net.liplum.animations.anis;

import mindustry.gen.Building;
import mindustry.world.Block;

public interface IRenderBehavior<TBlock extends Block, TBuild extends Building> {
    /**
     * How to render the {@code building}
     *
     * @param block the block of {@code building}
     * @param build the building to be rendered
     */
    void drawBuild(TBlock block, TBuild build);
}