package net.liplum.animations.anis;

import arc.util.Nullable;
import mindustry.gen.Building;
import mindustry.world.Block;

public class AniState<TBlock extends Block, TBuild extends Building> {
    private final String stateName;
    @Nullable
    private final IRenderBehavior<TBlock, TBuild> renderBehavior;
    public boolean overwriteBlock = false;

    public AniState(String stateName) {
        this.stateName = stateName;
        this.renderBehavior = null;
    }

    public AniState(String stateName, IRenderBehavior<TBlock, TBuild> rb) {
        this.stateName = stateName;
        this.renderBehavior = rb;
    }

    public String getStateName() {
        return stateName;
    }

    public void drawBuilding(TBlock block, TBuild build) {
        if (this.renderBehavior != null) {
            this.renderBehavior.drawBuilding(block, build);
        }
    }

    @Override
    public int hashCode() {
        return stateName.hashCode();
    }

    @Override
    public String toString() {
        return stateName;
    }
}
