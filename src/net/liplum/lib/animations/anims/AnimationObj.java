package net.liplum.lib.animations.anims;

import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import net.liplum.lib.ITimer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AnimationObj implements IAnimated, ITimer {
    @NotNull
    public final Animation meta;
    public float curTime;
    public int curTurn;
    public int turnWhenFallAsleep = -1;
    @Nullable
    public ITimeModifier timeModifier;
    @Nullable
    public ISleepModifier sleepModifier;
    @Nullable
    public IFrameIndexerObj indexer;
    public boolean needSleep;
    public boolean isAsleep;

    public AnimationObj(@NotNull Animation meta) {
        this.meta = meta;
    }

    @NotNull
    public TextureRegion get(int index) {
        return meta.allFrames[index];
    }

    public int getIndex(int length) {
        if (isAsleep) {
            return -1;
        }
        if (indexer != null) {
            return indexer.getCurIndex(this, length);
        } else {
            float process = curTime / meta.duration;
            return (int) (process * (length - 1));
        }
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
    @Override
    public void spend(float time) {
        if (!isAsleep) {
            curTime += Math.max(0f, getRealSpent(time));
            float duration = meta.duration;
            if (curTime > duration) {
                curTurn += curTime / duration;
                curTime %= duration;
            }
            if (needSleep && curTurn >= turnWhenFallAsleep) {
                isAsleep = true;
            }
        }
    }

    @NotNull
    public AnimationObj clearTime() {
        curTime = 0;
        curTurn = 0;
        turnWhenFallAsleep = -1;
        return this;
    }

    @NotNull
    public AnimationObj reset() {
        clearTime();
        wakeUp();
        return this;
    }

    @NotNull
    public AnimationObj tmod(@Nullable ITimeModifier tmod) {
        this.timeModifier = tmod;
        return this;
    }

    @NotNull
    public AnimationObj smod(@Nullable ISleepModifier smod) {
        this.sleepModifier = smod;
        return this;
    }

    @NotNull
    public AnimationObj indexer(@Nullable IFrameIndexerObj indexer) {
        this.indexer = indexer;
        return this;
    }

    @NotNull
    public AnimationObj wakeUp() {
        needSleep = false;
        isAsleep = false;
        return this;
    }

    public int getFallAsleepTurn() {
        if (sleepModifier != null) {
            return sleepModifier.modifier(this);
        } else {
            return curTurn + 1;
        }
    }

    @NotNull
    public AnimationObj sleep() {
        needSleep = true;
        turnWhenFallAsleep = getFallAsleepTurn();
        return this;
    }

    @NotNull
    public AnimationObj sleepInstantly() {
        needSleep = true;
        isAsleep = true;
        turnWhenFallAsleep = curTurn;
        return this;
    }

    @Override
    public void draw(float x, float y, float rotation) {
        if (!isAsleep) {
            meta.draw(this, x, y, rotation);
        }
    }

    @Override
    public void draw(@NotNull Color color, float x, float y, float rotation) {
        if (!isAsleep) {
            meta.draw(this, color, x, y, rotation);
        }
    }

    @Override
    public void draw(@NotNull IHowToRender howToRender) {
        if (!isAsleep) {
            meta.draw(this, howToRender);
        }
    }

    @Override
    public void draw(@NotNull IFrameIndexer indexer, @NotNull IHowToRender howToRender) {
        if (!isAsleep) {
            meta.draw(indexer, howToRender);
        }
    }
}
