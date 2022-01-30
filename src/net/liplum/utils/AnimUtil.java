package net.liplum.utils;

import mindustry.ctype.MappableContent;
import net.liplum.animations.anims.AutoAnimation;

public class AnimUtil {
    public static AutoAnimation auto(MappableContent content, String subName, int frame, float duration) {
        return new AutoAnimation(duration, AtlasUtil.animation(content, subName, frame));
    }

    public static AutoAnimation autoCio(String name, int frame, float duration) {
        return new AutoAnimation(duration, AtlasUtil.animationCio(name, frame));
    }
}
