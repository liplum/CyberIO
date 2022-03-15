package net.liplum.utils;

import mindustry.Vars;
import mindustry.world.Block;

public class WorldU {
    public static float toDrawXY(Block block, short blockXY) {
        return block.offset + blockXY * Vars.tilesize;
    }

    public static float toDrawXY(Block block, int blockXY) {
        return block.offset + blockXY * Vars.tilesize;
    }

    public static float toDrawXY(short blockXY) {
        return blockXY * Vars.tilesize;
    }

    public static float toDrawXY(int blockXY) {
        return blockXY * Vars.tilesize;
    }
}
