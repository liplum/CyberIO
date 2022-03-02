package net.liplum.animations.anims.blocks;

import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.util.Time;
import net.liplum.animations.anims.Animation;
import net.liplum.animations.anims.IFrameIndexer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A simple animation whose frames have the same duration and. Animation is played based on {@link Time#time}
 */
public class AutoAnimation extends Animation {
    public final float totalDuration;

    /**
     * @param totalDuration how long can this animation be played
     * @param allFrames     every frame which has the same duration
     */
    public AutoAnimation(float totalDuration, TextureRegion... allFrames) {
        super(totalDuration, allFrames);
        this.totalDuration = Math.max(1, totalDuration);
    }

    @Override
    public int getCurIndex(int length) {
        float progress = Time.time % totalDuration / totalDuration;//percent
        int index = (int) (progress * length);
        index = Mathf.clamp(index, 0, length);
        return index;
    }

    @NotNull
    @Override
    public AutoAnimation indexer(@Nullable IFrameIndexer indexer) {
        this.indexer = indexer;
        return this;
    }
}
