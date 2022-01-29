package net.liplum.animations;

public interface IRenderBehavior<TBlock, TBuild> {
    void drawBuilding(TBlock block, TBuild build);
}