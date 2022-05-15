package net.liplum.api.virus;

import mindustry.world.Block;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.BlockGroup;

import java.util.HashSet;
import java.util.Set;

public class UninfectedBlocksRegistry {
    private static final Set<Block> UninfectedBlocks = new HashSet<>();
    private static final Set<Floor> UninfectedOverlays = new HashSet<>();
    private static final Set<Floor> UninfectedFloors = new HashSet<>();
    private static final Set<BlockFlag> UninfectedFlag = new HashSet<>();
    private static final Set<BlockGroup> UninfectedGroup = new HashSet<>();

    public static boolean block(Block block) {
        return UninfectedBlocks.add(block);
    }

    public static boolean floor(Floor floor) {
        return UninfectedFloors.add(floor);
    }

    public static boolean floor(Block floor) {
        return UninfectedFloors.add(floor.asFloor());
    }

    public static boolean overlay(Floor overlay) {
        return UninfectedOverlays.add(overlay);
    }

    public static boolean overlay(Block overlay) {
        return UninfectedOverlays.add(overlay.asFloor());
    }

    public static boolean flag(BlockFlag flag) {
        return UninfectedFlag.add(flag);
    }

    public static boolean group(BlockGroup group) {
        return UninfectedGroup.add(group);
    }

    public static boolean hasBlock(Block block) {
        return UninfectedBlocks.contains(block);
    }

    public static boolean hasFloor(Floor floor) {
        return UninfectedFloors.contains(floor);
    }

    public static boolean hasOverlay(Floor overlay) {
        return UninfectedOverlays.contains(overlay);
    }

    public static boolean hasFloor(Block floor) {
        return UninfectedFloors.contains(floor.asFloor());
    }

    public static boolean hasOverlay(Block overlay) {
        return UninfectedOverlays.contains(overlay.asFloor());
    }

    public static boolean hasFlag(BlockFlag flag) {
        return UninfectedFlag.contains(flag);
    }

    public static boolean hasGroup(BlockGroup group) {
        return UninfectedGroup.contains(group);
    }
}
