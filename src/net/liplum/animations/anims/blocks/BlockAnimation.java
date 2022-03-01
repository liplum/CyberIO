package net.liplum.animations.anims.blocks;

import arc.graphics.g2d.TextureRegion;
import net.liplum.animations.anims.Animation;
import net.liplum.animations.anims.IFrameIndexer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockAnimation extends Animation {

    /**
     * @param allFrames every frame which has the same duration
     */
    public BlockAnimation(TextureRegion... allFrames) {
        super(allFrames);
    }

    @NotNull
    @Override
    public BlockAnimation indexer(@Nullable IFrameIndexer indexer) {
        this.indexer = indexer;
        return this;
    }
}
