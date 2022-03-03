package net.liplum.utils;

import mindustry.ctype.MappableContent;
import net.liplum.animations.anims.Animation;
import net.liplum.animations.anims.AnimationH;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AnimU {

    public static Animation auto(MappableContent content, @Nullable String subName, int frame, float totalDuration) {
        return AnimationH.AutoAnimation(totalDuration, AtlasU.animation(content, subName, frame));
    }

    public static Animation autoCio(@NotNull String name, int frame, float totalDuration) {
        return AnimationH.AutoAnimation(totalDuration, AtlasU.animationCio(name, frame));
    }
}
