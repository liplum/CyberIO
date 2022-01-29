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

    public static TextureRegion[] animation(Block block, String subName, int number) {
        return animation(block, subName, true, number);
    }

    public static TextureRegion[] animation(Block block, String subName, boolean isHorizontal, int number) {
        String identity = block.name + '-' + subName + "-anim";
        TextureRegion tr = Core.atlas.find(identity);
        return slice(tr, isHorizontal, number);
    }

    public static TextureRegion[] slice(TextureRegion original, boolean isHorizontal, int count) {
        TextureRegion[] fms = new TextureRegion[count];
        int width = original.width;
        int height = original.height;
        if (isHorizontal) {
            TextureRegion[][] split = original.split(width / count, height);
            for (int i = 0; i < count; i++) {
                fms[i] = split[i][0];
            }
        } else {
            TextureRegion[][] split = original.split(width, height / count);
            for (int i = 0; i < count; i++) {
                fms[i] = split[0][i];
            }
        }
        return fms;
    }
}
