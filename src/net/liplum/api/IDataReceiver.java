package net.liplum.api;

import mindustry.type.Item;

public interface IDataReceiver {
    boolean acceptData(IDataSender sender, Item item);

    void receiveData(IDataSender sender, Item item);
}
