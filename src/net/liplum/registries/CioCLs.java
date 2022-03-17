package net.liplum.registries;

import static  mindustry.graphics.CacheLayer.*;

import mindustry.graphics.CacheLayer;
import  net.liplum.ClientOnly;
public class CioCLs {
    @ClientOnly
    public static CacheLayer dynamicColor;

    @ClientOnly
    public static CacheLayer hologram;

    public static void load() {
        dynamicColor = new ShaderLayer(CioShaders.dynamicColor);
        hologram = new ShaderLayer(CioShaders.hologram);
    }
}
