package net.liplum.api.data;

import mindustry.type.Item;

public interface IDataSender extends IDataBuilding {
    void sendData(IDataReceiver receiver, Item item, int amount);
}
