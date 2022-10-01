package net.liplum.blocks.virus;

import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.meta.BlockFlag;
import net.liplum.api.virus.UninfectedBlocksRegistry;

public class VirusU {
    public static boolean canInfect(Tile tile) {
        if (tile == null) {
            return false;
        }
        Block block = tile.block();
        if (block instanceof Virus) {
            return false;
        }
        if (block instanceof CoreBlock) {
            return false;
        }
        Floor floor = tile.floor();
        Floor overlay = tile.overlay();
        if (UninfectedBlocksRegistry.hasFloor(floor) ||
            UninfectedBlocksRegistry.hasOverlay(overlay) ||
            UninfectedBlocksRegistry.hasBlock(block) ||
            UninfectedBlocksRegistry.hasGroup(block.group)
        ) {
            return false;
        }

        for (BlockFlag flag : block.flags.array) {
            if (UninfectedBlocksRegistry.hasFlag(flag)) {
                return false;
            }
        }
        return true;
    }
}