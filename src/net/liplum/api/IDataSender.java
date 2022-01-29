package net.liplum.api;

import mindustry.type.Item;

public interface IDataSender extends IDataBuilding{
    void sendData(IDataReceiver receiver, Item item);
}
