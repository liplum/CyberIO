package net.liplum.api.data;

import arc.struct.OrderedSet;
import mindustry.type.Item;
import net.liplum.SendDataPack;
import net.liplum.utils.ArcU;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IDataSender extends IDataBuilding {
    default int sendData(@NotNull IDataReceiver receiver, @NotNull Item item, int amount) {
        int maxAccepted = receiver.acceptedAmount(this, item);
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

    @SendDataPack
    void connectSync(@NotNull IDataReceiver receiver);

    @SendDataPack
    void disconnectSync(@NotNull IDataReceiver receiver);

    @Nullable
    Integer connectedReceiver();

    default boolean canMultipleConnect() {
        return maxReceiverConnection() != 1;
    }

    default boolean isConnectedWith(@NotNull IDataReceiver receiver) {
        if (canMultipleConnect()) {
            return connectedReceivers().contains(receiver.getBuilding().pos());
        } else {
            Integer connected = connectedReceiver();
            if (connected == null) {
                return false;
            } else {
                return connected == receiver.getBuilding().pos();
            }
        }
    }

    /**
     * Gets the maximum limit of connection.<br/>
     * -1 : unlimited
     * @return the maximum of connection
     */
    default int maxReceiverConnection() {
        return 1;
    }

    @NotNull
    default OrderedSet<Integer> connectedReceivers() {
        return ArcU.emptySet();
    }
}
