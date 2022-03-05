package net.liplum.animations.anims;

import arc.graphics.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AnimationObj implements IAnimated {
    @NotNull
    public final Animation meta;
    public float curTime;
    @Nullable
    public ITimeModifier timeModifier;

    public AnimationObj(@NotNull Animation meta) {
        this.meta = meta;
    }

    public int getIndex(int length) {
        float process = curTime / meta.duration;
        return (int) (process * length);
    }

    public float getRealSpent(float time) {
        if (timeModifier != null) {
            return timeModifier.modify(time);
        }
        return time;
    }

    /**
     * Spend some time.
     *
     * @param time spent time
     */
    public void spend(float time) {
        curTime += getRealSpent(time);
        if (curTime > meta.duration) {
            curTime %= meta.duration;
        } else if (curTime < 0f) {
            curTime = meta.duration - Math.abs(curTime);
        }
    }

    @NotNull
    public AnimationObj tmod(@Nullable ITimeModifier tmod) {
        this.timeModifier = tmod;
        return this;
    }

    @Override
    public void draw(float x, float y, float rotation) {
        meta.draw(this, x, y, rotation);
    }

    @Override
    public void draw(@NotNull Color color, float x, float y, float rotation) {
        meta.draw(this, color, x, y, rotation);
    }

    @Override
    public void draw(@NotNull IHowToRender howToRender) {
        meta.draw(this, howToRender);
    }

    @Override
    public void draw(@NotNull IFrameIndexer indexer, @NotNull IHowToRender howToRender) {
        meta.draw(indexer, howToRender);
    }
}
