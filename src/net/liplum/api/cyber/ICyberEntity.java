package net.liplum.api.cyber;

import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.Tile;
import org.jetbrains.annotations.NotNull;

public interface ICyberEntity {
    @NotNull
    Building getBuilding();

    @NotNull
    Tile getTile();

    @NotNull
    Block getBlock();
}
