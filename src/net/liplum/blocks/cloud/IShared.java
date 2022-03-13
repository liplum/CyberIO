package net.liplum.blocks.cloud;

import mindustry.gen.Building;
import mindustry.world.modules.ItemModule;
import org.jetbrains.annotations.NotNull;

public interface IShared {
    @NotNull
    ItemModule getSharedItems();

    void setSharedItems(@NotNull ItemModule itemModule);

    @NotNull
    Building getBuilding();

    @NotNull
    CloudInfo getSharedInfo();

    void setSharedInfo(@NotNull CloudInfo info);
}
