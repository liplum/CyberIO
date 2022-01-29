package net.liplum.utils;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import mindustry.world.Block;

public class AtlasUtil {
    public static TextureRegion sub(Block block, String subName) {
        return Core.atlas.find(block.name + '-' + subName);
    }

    public static TextureRegion[] subFrames(Block block, String subName, int number) {
        return subFrames(block, subName, 0, number);
    }

    public static TextureRegion[] subFrames(Block block, String subName, int start, int number) {
        TextureRegion[] fms = new TextureRegion[number];
        int end = number + start;
        for (int i = start; i < end; i++) {
            fms[i - start] = Core.atlas.find(block.name + "-" + subName + "-" + i);
        }
        return fms;
    }
}
