package net.liplum.api.cyber;

import arc.struct.ObjectSet;
import mindustry.type.Item;
import net.liplum.SendDataPack;
import net.liplum.utils.ArcU;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IDataSender extends IDataBuilding {
    /**
     * sends items
     *
     * @param receiver the target who receives the item(s)
     * @param item     which kind of item will be sent soon
     * @param amount   how many item(s) will be sent
     * @return the rest of item(s)
     */
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

    @SendDataPack
    default void connectSync(int receiver) {
        IDataReceiver dr = CyberH.dr(receiver);
        if (dr != null) {
            connectSync(dr);
        }
    }

    @SendDataPack
    default void disconnectSync(int receiver) {
        IDataReceiver dr = CyberH.dr(receiver);
        if (dr != null) {
            disconnectSync(dr);
        }
    }

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
     *
     * @return the maximum of connection
     */
    default int maxReceiverConnection() {
        return 1;
    }

    default boolean canHaveMoreReceiverConnection() {
        int max = maxReceiverConnection();
        if (max == -1) {
            return true;
        }
        return connectedReceivers().size < max;
    }

    default int getReceiverConnectionNumber() {
        if (canMultipleConnect()) {
            return connectedReceivers().size;
        } else {
            return connectedReceiver() == null ? 0 : 1;
        }
    }

    @NotNull
    default ObjectSet<Integer> connectedReceivers() {
        return ArcU.emptySet();
    }
}
