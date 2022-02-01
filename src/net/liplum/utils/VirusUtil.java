package net.liplum.utils;

import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.storage.CoreBlock;
import net.liplum.api.virus.IAntiVirused;
import net.liplum.api.virus.UninfectedBlocks;
import net.liplum.blocks.virus.Virus;

public class VirusUtil {
    public static boolean canInfect(Tile tile) {
        if (tile == null) {
            return false;
        }
        Block block = tile.block();
        Floor floor = tile.floor();
        Floor overlay = tile.overlay();
        if (UninfectedBlocks.canInfectFloor(floor) ||
                UninfectedBlocks.canInfectOverlay(overlay) ||
                UninfectedBlocks.canInfectBlock(block)
        ) {
            return false;
        }
        if (block instanceof Virus) {
            return false;
        }
        if (block instanceof CoreBlock) {
            return false;
        }
        if (block instanceof IAntiVirused) {
            return false;
        }
        return true;
    }
}
