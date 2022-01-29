package net.liplum.utils;

import mindustry.world.Block;
import net.liplum.animations.AutoAnimation;

public class AnimUtil {
    public static AutoAnimation autoAnimation(Block block, String subName, int frame, float duration) {
        return new AutoAnimation(duration, AtlasUtil.animation(block, subName, frame));
    }
}
