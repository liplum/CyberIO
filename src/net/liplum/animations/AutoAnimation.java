package net.liplum.animations;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.util.Time;

public class AutoAnimation implements IAnimated {
    private final TextureRegion[] allFrames;
    private final float totalDuration;

    public AutoAnimation(float totalDuration, TextureRegion... allFrames) {
        this.totalDuration = Math.max(1, totalDuration);
        this.allFrames = allFrames;
    }

    public TextureRegion getCurTR() {
        float progress = Time.time % totalDuration / totalDuration;//percent
        int index = (int) (progress * allFrames.length);
        index = Mathf.clamp(index, 0, allFrames.length);
        return allFrames[index];
    }

    @Override
    public void draw(float x, float y) {
        Draw.rect(getCurTR(), x, y);
    }

    @Override
    public void draw(Color color, float x, float y) {
        Draw.color(color);
        Draw.rect(getCurTR(), x, y);
        Draw.color();
    }
}
