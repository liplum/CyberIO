package net.liplum.api.data;

import mindustry.type.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IDataSender extends IDataBuilding {
    void sendData(@NotNull IDataReceiver receiver, @NotNull Item item, int amount);

    @Nullable
    Integer connectedReceiver();
}
