package net.liplum.api.stream;

import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.Tile;
import org.jetbrains.annotations.NotNull;

public interface IStreamNode {
    @NotNull
    Building getBuilding();

    @NotNull
    Tile getTile();

    @NotNull
    Block getBlock();
}