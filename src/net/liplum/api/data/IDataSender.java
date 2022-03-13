package net.liplum.api.data;

import arc.struct.OrderedSet;
import mindustry.type.Item;
import net.liplum.utils.ArcU;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IDataSender extends IDataBuilding {
    default int sendData(@NotNull IDataReceiver receiver, @NotNull Item item, int amount) {
        int maxAccepted = receiver.acceptedAmount(this,item);
        if (maxAccepted == -1) {
            receiver.receiveData(this, item, amount);
            return 0;
        }
        if (maxAccepted >= amount) {
            receiver.receiveData(this, item, amount);
            return 0;
        } else {
            int rest = amount - maxAccepted;
            receiver.receiveData(this, item, maxAccepted);
            return rest;
        }
    }

    void connect(@NotNull IDataReceiver receiver);

    void disconnect(@NotNull IDataReceiver receiver);

    @Nullable
    Integer connectedReceiver();

    default boolean canMultipleConnect() {
        return false;
    }

    @NotNull
    default OrderedSet<Integer> connectedReceivers() {
        return ArcU.emptySet();
    }
}
