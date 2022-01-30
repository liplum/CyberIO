package net.liplum.animations.anis;

public interface IRenderBehavior<TBlock, TBuild> {
    void drawBuilding(TBlock block, TBuild build);
}