package net.liplum.utils;

import mindustry.ctype.MappableContent;
import net.liplum.animations.anims.AutoAnimation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AnimU {
    public static AutoAnimation auto(MappableContent content, @Nullable String subName, int frame, float totalDuration) {
        return new AutoAnimation(totalDuration, AtlasU.animation(content, subName, frame));
    }

    public static AutoAnimation autoCio(@NotNull String name, int frame, float totalDuration) {
        return new AutoAnimation(totalDuration, AtlasU.animationCio(name, frame));
    }
}
