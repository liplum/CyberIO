package net.liplum.utils;

import mindustry.Vars;
import mindustry.world.Block;

public class WorldUtil {
    public static float toDrawXY(Block block, int blockXY) {
        return block.offset + blockXY * Vars.tilesize;
    }
}
