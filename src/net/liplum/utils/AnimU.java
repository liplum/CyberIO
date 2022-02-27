package net.liplum.utils;

import mindustry.ctype.MappableContent;
import net.liplum.animations.anims.blocks.AutoAnimationT;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AnimU {

    public static AutoAnimationT auto(MappableContent content, @Nullable String subName, int frame, float totalDuration) {
        return new AutoAnimationT(totalDuration, AtlasU.animation(content, subName, frame));
    }

    public static AutoAnimationT autoCio(@NotNull String name, int frame, float totalDuration) {
        return new AutoAnimationT(totalDuration, AtlasU.animationCio(name, frame));
    }
}
