package net.liplum.api.virus;

import mindustry.world.Block;
import mindustry.world.blocks.environment.Floor;

import java.util.HashSet;
import java.util.Set;

public class UninfectedBlocks {
    private static final Set<Block> UninfectedBlocks = new HashSet<>();
    private static final Set<Floor> UninfectedOverlays = new HashSet<>();
    private static final Set<Floor> UninfectedFloors = new HashSet<>();

    public static boolean registerBlock(Block block) {
        return UninfectedBlocks.add(block);
    }

    public static boolean registerFloor(Floor floor) {
        return UninfectedFloors.add(floor);
    }

    public static boolean registerOverlay(Floor overlay) {
        return UninfectedOverlays.add(overlay);
    }


    public static boolean canInfectBlock(Block block) {
        return UninfectedBlocks.contains(block);
    }

    public static boolean canInfectFloor(Floor floor) {
        return UninfectedFloors.contains(floor);
    }

    public static boolean canInfectOverlay(Floor overlay) {
        return UninfectedOverlays.contains(overlay);
    }

}
