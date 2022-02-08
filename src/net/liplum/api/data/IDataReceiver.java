package net.liplum.api.data;

import mindustry.type.Item;

public interface IDataReceiver extends IDataBuilding {
    boolean acceptData(IDataSender sender, Item item);

    void receiveData(IDataSender sender, Item item, int amount);

    boolean canAcceptAnyData(IDataSender sender);

    boolean isOutputting();

    void connect(IDataSender sender);

    void disconnect(IDataSender sender);
}
