package net.liplum.utils;

import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.storage.CoreBlock;
import net.liplum.api.virus.UninfectedBlocksRegistry;
import net.liplum.blocks.virus.Virus;

public class VirusUtil {
    public static boolean canInfect(Tile tile) {
        if (tile == null) {
            return false;
        }
        Block block = tile.block();
        Floor floor = tile.floor();
        Floor overlay = tile.overlay();
        if (UninfectedBlocksRegistry.hasFloor(floor) ||
                UninfectedBlocksRegistry.hasOverlay(overlay) ||
                UninfectedBlocksRegistry.hasBlock(block)
        ) {
            return false;
        }
        if (block instanceof Virus) {
            return false;
        }
        if (block instanceof CoreBlock) {
            return false;
        }
        return true;
    }
}
