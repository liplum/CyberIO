package net.liplum.utils;

import mindustry.ctype.MappableContent;
import net.liplum.animations.anims.Animation;
import net.liplum.animations.anims.blocks.AutoAnimation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AnimU {

    public static Animation auto(MappableContent content, @Nullable String subName, int frame, float totalDuration) {
        return new AutoAnimation(totalDuration, AtlasU.animation(content, subName, frame));
    }

    public static Animation autoCio(@NotNull String name, int frame, float totalDuration) {
        return new AutoAnimation(totalDuration, AtlasU.animationCio(name, frame));
    }
}
