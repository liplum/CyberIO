package net.liplum.api;

import mindustry.type.Item;

public interface IDataSender {
    void sendData(IDataReceiver receiver, Item item);
}
