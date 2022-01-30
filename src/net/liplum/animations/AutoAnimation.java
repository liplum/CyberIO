package net.liplum.animations;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.util.Nullable;
import arc.util.Time;
import mindustry.gen.Building;

public class AutoAnimation implements IAnimated {
    public final TextureRegion[] allFrames;
    public final float totalDuration;

    public AutoAnimation(float totalDuration, TextureRegion... allFrames) {
        this.totalDuration = Math.max(1, totalDuration);
        this.allFrames = allFrames;
    }

    public float getFixedTotalDuration(@Nullable Building tileEntity) {
        if (tileEntity == null) {
            return totalDuration;
        } else {
            return totalDuration / tileEntity.timeScale();
        }
    }

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
