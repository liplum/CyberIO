package net.liplum.data;

import mindustry.world.modules.ItemModule;
import net.liplum.api.ICyberEntity;
import org.jetbrains.annotations.NotNull;

public interface IShared extends ICyberEntity {
    @NotNull
    ItemModule getSharedItems();

    void setSharedItems(@NotNull ItemModule itemModule);

    @NotNull
    CloudInfo getSharedInfo();

    void setSharedInfo(@NotNull CloudInfo info);
}
