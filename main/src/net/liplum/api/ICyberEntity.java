package net.liplum.api;

import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.Tile;
import org.jetbrains.annotations.NotNull;

public interface ICyberEntity {
    @NotNull
    default Building getBuilding() {
        return (Building) this;
    }

    @NotNull
    default Tile getTile() {
        return getBuilding().tile;
    }

    default boolean isFlying() {
        return getTile().equals(Vars.emptyTile);
    }

    @NotNull
    default Block getBlock() {
        return getBuilding().block;
    }
}
