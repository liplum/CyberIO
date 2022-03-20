package net.liplum.utils;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import arc.util.Log;
import mindustry.Vars;
import mindustry.ctype.MappableContent;
import net.liplum.Meta;
import org.jetbrains.annotations.Nullable;

public class AtlasU {
    public static TextureRegion sub(MappableContent content, String subName) {
        return Core.atlas.find(content.name + '-' + subName);
    }

    public static TextureRegion inMod(MappableContent content, String name) {
        return Core.atlas.find(content.minfo.mod.name + '-' + name);
    }

    public static TextureRegion inMod(String name) {
        return Core.atlas.find(Vars.content.transformName(name));
    }

    public static TextureRegion inCio(String name) {
        return Core.atlas.find(Meta.ModID + '-' + name);
    }

    public static TextureRegion[] subFrames(MappableContent content, @Nullable String subName, int number) {
        return subFrames(content, subName, 0, number);
    }

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
            fms[i - start] = Core.atlas.find(identity + "-" + i);
        }
        return fms;
    }

    public static TextureRegion[] animation(MappableContent content, @Nullable String subName, int number) {
        return animation(content, subName, true, number);
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
        return slice(tr, isHorizontal, number);
    }

    public static TextureRegion[] sheet(MappableContent content, @Nullable String subName, int number) {
        return sheet(content, subName, true, number);
    }

    public static TextureRegion[] sheet(MappableContent content, @Nullable String subName, boolean isHorizontal, int number) {
        String identity;
        if (subName != null) {
            identity = content.name + '-' + subName;
        } else {
            identity = content.name;
        }
        TextureRegion tr = Core.atlas.find(identity);
        return slice(tr, isHorizontal, number);
    }

    public static TextureRegion[] animationCio(String name, int number) {
        return animationCio(name, true, number);
    }

    public static TextureRegion[] animationCio(String name, boolean isHorizontal, int number) {
        String identity = Meta.ModID + '-' + name + "-anim";
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
