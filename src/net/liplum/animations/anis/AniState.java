package net.liplum.animations.anis;

import arc.util.Nullable;
import mindustry.gen.Building;
import mindustry.world.Block;

/**
 * The animation state which decide how to render
 *
 * @param <TBlock> the type of block which has this animation state
 * @param <TBuild> the corresponding {@link Building} type
 */
public class AniState<TBlock extends Block, TBuild extends Building> {
    private final String stateName;
    @Nullable
    private final IRenderBehavior<TBlock, TBuild> renderBehavior;
    private boolean overwriteBlock = false;

    /**
     * @param stateName a name
     */
    public AniState(String stateName) {
        this.stateName = stateName;
        this.renderBehavior = null;
    }

    /**
     * @param stateName a name
     * @param rb        how to render
     */
    public AniState(String stateName, IRenderBehavior<TBlock, TBuild> rb) {
        this.stateName = stateName;
        this.renderBehavior = rb;
    }

    /**
     * Gets the name
     *
     * @return name
     */
    public String getStateName() {
        return stateName;
    }

    /**
     * Renders the current image
     * @param block the block of {@code build}
     * @param build the subject to be rendered
     */
    public void drawBuilding(TBlock block, TBuild build) {
        if (this.renderBehavior != null) {
            this.renderBehavior.drawBuilding(block, build);
        }
    }

    public boolean isOverwriteBlock() {
        return overwriteBlock;
    }

    public AniState<TBlock, TBuild> setOverwriteBlock(boolean overwriteBlock) {
        this.overwriteBlock = overwriteBlock;
        return this;
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
