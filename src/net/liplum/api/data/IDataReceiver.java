package net.liplum.api.data;

import arc.struct.ObjectSet;
import mindustry.type.Item;
import net.liplum.CalledBySync;
import net.liplum.ClientOnly;
import net.liplum.delegates.Delegate1;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IDataReceiver extends IDataBuilding {
    void receiveData(@NotNull IDataSender sender, @NotNull Item item, int amount);

    /**
     * Gets the max acceptable number of this {@code item}.
     * -1 means any
     *
     * @param sender sender
     * @param itme   item
     * @return amount
     */
    int acceptedAmount(@NotNull IDataSender sender, @NotNull Item itme);

    @ClientOnly
    boolean canAcceptAnyData(@NotNull IDataSender sender);

    /**
     * Gets what this receiver wants<br/>
     * null : Any<br/>
     * Array.Empty : Nothing<br/>
     * An array : what's in the array<br/>
     *
     * @return what this receiver wants
     */
    @Nullable
    Item[] getRequirements();

    @ClientOnly
    boolean isBlocked();

    @CalledBySync
    void connect(@NotNull IDataSender sender);

    @CalledBySync
    void disconnect(@NotNull IDataSender sender);

    @NotNull
    ObjectSet<Integer> connectedSenders();

    default boolean isConnectedWith(@NotNull IDataSender sender) {
        return connectedSenders().contains(sender.getBuilding().pos());
    }

    @NotNull
    Delegate1<IDataReceiver> getOnRequirementUpdated();

    @Nullable
    Integer connectedSender();

    int maxSenderConnection();

    boolean acceptConnection(@NotNull IDataSender sender);
}
