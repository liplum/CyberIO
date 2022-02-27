package net.liplum.animations.anims.blocks;

import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.util.Time;
import mindustry.gen.Building;
import net.liplum.animations.anims.IFrameIndexer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A simple animation whose frames have the same duration.
 */
public class AutoAnimation extends BlockAnimation {
    public final float totalDuration;

    /**
     * @param totalDuration how long can this animation be played
     * @param allFrames     every frame which has the same duration
     */
    public AutoAnimation(float totalDuration, TextureRegion... allFrames) {
        super(allFrames);
        this.totalDuration = Math.max(1, totalDuration);
        this.indexer = (length, data) -> {
            if (length == 0) {
                return -1;
            }
            float fixedTotalDuration = getFixedTotalDuration(data);
            float progress = Time.time % fixedTotalDuration / fixedTotalDuration;//percent
            int index = (int) (progress * length);
            index = Mathf.clamp(index, 0, length);
            return index;
        };
    }

    /**
     * Gets the real duration of this
     *
     * @param tileEntity if isn't a null, the result will base on {@link Building#timeScale()}
     * @return total duration how long this animation can be played
     */
    public float getFixedTotalDuration(@Nullable Building tileEntity) {
        if (tileEntity == null) {
            return totalDuration;
        } else {
            return totalDuration / tileEntity.timeScale();
        }
    }

    @NotNull
    @Override
    public AutoAnimation indexer(@Nullable IFrameIndexer<Building> indexer) {
        return this;
    }
}
