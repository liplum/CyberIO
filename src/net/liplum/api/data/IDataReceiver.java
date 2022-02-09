package net.liplum.api.data;

import arc.struct.ObjectSet;
import mindustry.type.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IDataReceiver extends IDataBuilding {
    boolean acceptData(@NotNull IDataSender sender, Item item);

    void receiveData(@NotNull IDataSender sender, Item item, int amount);

    boolean canAcceptAnyData(@NotNull IDataSender sender);

    boolean isOutputting();

    void connect(@NotNull IDataSender sender);

    void disconnect(@NotNull IDataSender sender);

    @NotNull
    ObjectSet<Integer> connectedSenders();

    @Nullable
    Integer connectedSender();

    boolean acceptConnection(@NotNull IDataSender sender);
}
