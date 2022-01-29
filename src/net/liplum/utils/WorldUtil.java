package net.liplum.utils;

import mindustry.Vars;
import mindustry.world.Block;

public class WorldUtil {
    public static float toDrawXY(Block block, float blockXY) {
        return block.offset + blockXY * Vars.tilesize;
    }
}
