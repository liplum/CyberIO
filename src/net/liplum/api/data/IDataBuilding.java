package net.liplum.api.data;

import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.Tile;
import org.jetbrains.annotations.NotNull;

public interface IDataBuilding {
    @NotNull
    Building getBuilding();

    @NotNull
    Tile getTile();

    @NotNull
    Block getBlock();
}
