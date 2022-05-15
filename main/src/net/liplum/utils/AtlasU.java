package net.liplum.utils;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import arc.util.Log;
import mindustry.ctype.MappableContent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AtlasU {
    public static TextureRegion[] subFrames(MappableContent content, @Nullable String subName, int start, int number) {
        TextureRegion[] fms = new TextureRegion[number];
        int end = number + start;
        String identity;
        if (subName != null) {
            identity = content.name + "-" + subName;
        } else {
            identity = content.name;
        }
        for (int i = start; i < end; i++) {
            fms[i - start] = Core.atlas.find(identity + i);
        }
        return fms;
    }

    public static TextureRegion[] animation(MappableContent content, @Nullable String subName, boolean isHorizontal, int number) {
        String identity;
        if (subName != null) {
            identity = content.name + '-' + subName + "-anim";
        } else {
            identity = content.name + "-anim";
        }
        TextureRegion tr = Core.atlas.find(identity);
        if (!Core.atlas.isFound(tr)) {
            String possibleName = identity.substring(0, identity.length() - 5);
            String possibility = Core.atlas.isFound(Core.atlas.find(possibleName)) ?
                    "Maybe it's " + possibleName + "?" :
                    "";
            Log.warn("Can't find texture[" + identity + "]." + possibility);
        }
        return slice(tr, number, isHorizontal);
    }

    public static TextureRegion[] sheet(@NotNull String identity, boolean isHorizontal, int number) {
        TextureRegion tr = Core.atlas.find(identity);
        return slice(tr, number, isHorizontal);
    }

    public static TextureRegion[] slice(TextureRegion original, int count, boolean isHorizontal) {
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
            System.arraycopy(split[0], 0, fms, 0, count);
        }
        return fms;
    }
}
