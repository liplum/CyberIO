package net.liplum.blocks.cloud;

import mindustry.world.modules.ItemModule;
import net.liplum.api.cyber.ICyberEntity;
import org.jetbrains.annotations.NotNull;

public interface IShared extends ICyberEntity {
    @NotNull
    ItemModule getSharedItems();

    void setSharedItems(@NotNull ItemModule itemModule);

    @NotNull
    CloudInfo getSharedInfo();

    void setSharedInfo(@NotNull CloudInfo info);
}
