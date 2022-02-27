package net.liplum.animations.anims.blocks;

import arc.graphics.g2d.TextureRegion;
import mindustry.gen.Building;
import net.liplum.animations.anims.Animation;
import net.liplum.animations.anims.IAnimatedBlock;
import net.liplum.animations.anims.IFrameIndexer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockAnimation extends Animation<Building> implements IAnimatedBlock {

    public BlockAnimation(@NotNull TextureRegion... allFrames) {
        super(allFrames);
    }

    @NotNull
    @Override
    public BlockAnimation indexer(@Nullable IFrameIndexer<Building> indexer) {
        this.indexer = indexer;
        return this;
    }
}
