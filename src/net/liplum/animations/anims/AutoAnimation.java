package net.liplum.animations.anims;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.util.Nullable;
import arc.util.Time;
import mindustry.gen.Building;

/**
 * A simple animation whose frames have the same duration.
 */
public class AutoAnimation implements IAnimated {
    public final TextureRegion[] allFrames;
    public final float totalDuration;

    /**
     * @param totalDuration how long can this animation be played
     * @param allFrames     every frame which has the same duration
     */
    public AutoAnimation(float totalDuration, TextureRegion... allFrames) {
        this.totalDuration = Math.max(1, totalDuration);
        this.allFrames = allFrames;
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

    /**
     * Gets current frame depending on {@code tileEntity}
     *
     * @param tileEntity which has this animation
     * @return the image of current frame
     */
    public TextureRegion getCurTR(@Nullable Building tileEntity) {
        float fixedTotalDuration = getFixedTotalDuration(tileEntity);
        float progress = Time.time % fixedTotalDuration / fixedTotalDuration;//percent
        int index = (int) (progress * allFrames.length);
        index = Mathf.clamp(index, 0, allFrames.length);
        return allFrames[index];
    }

    @Override
    public void draw(float x, float y, @Nullable Building tileEntity) {
        Draw.rect(getCurTR(tileEntity), x, y);
    }

    @Override
    public void draw(Color color, float x, float y, @Nullable Building tileEntity) {
        Draw.color(color);
        Draw.rect(getCurTR(tileEntity), x, y);
        Draw.color();
    }
}
