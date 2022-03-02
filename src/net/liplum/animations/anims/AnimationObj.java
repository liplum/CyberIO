package net.liplum.animations.anims;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AnimationObj {
    public final Animation meta;
    public float curTime;
    @Nullable
    public ITimeModifier timeModifier;

    public AnimationObj(Animation meta) {
        this.meta = meta;
    }

    /**
     * Spend some time.
     *
     * @param time spent time
     */
    void spend(float time) {
        curTime += timeModifier != null ? timeModifier.modify(time) : time;
        if (curTime > meta.duration) {
            curTime %= meta.duration;
        }
    }

    @NotNull
    public AnimationObj tmod(@Nullable ITimeModifier tmod) {
        this.timeModifier = tmod;
        return this;
    }
}
