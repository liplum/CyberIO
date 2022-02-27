package net.liplum.utils;

import mindustry.ctype.MappableContent;
import mindustry.gen.Building;
import net.liplum.animations.anims.blocks.AutoAnimationT;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AnimU {

    public static AutoAnimationT<Building> auto(MappableContent content, @Nullable String subName, int frame, float totalDuration) {
        return new AutoAnimationT<>(totalDuration, AtlasU.animation(content, subName, frame));
    }

    public static AutoAnimationT<Building> autoCio(@NotNull String name, int frame, float totalDuration) {
        return new AutoAnimationT<>(totalDuration, AtlasU.animationCio(name, frame));
    }

    public static <T extends Building> AutoAnimationT<T> autoT(MappableContent content, @Nullable String subName, int frame, float totalDuration) {
        return new AutoAnimationT<>(totalDuration, AtlasU.animation(content, subName, frame));
    }

    public static <T extends Building> AutoAnimationT<T> autoCioT(@NotNull String name, int frame, float totalDuration) {
        return new AutoAnimationT<>(totalDuration, AtlasU.animationCio(name, frame));
    }
}
