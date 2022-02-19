package net.liplum.animations.anis;

public interface IRenderBehavior<TBlock, TBuild> {
    /**
     * How to render the {@code building}
     * @param block the block of {@code building}
     * @param build the building to be rendered
     */
    void drawBuilding(TBlock block, TBuild build);
}