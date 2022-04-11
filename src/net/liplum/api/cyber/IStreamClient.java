package net.liplum.api.cyber;

import arc.graphics.Color;
import arc.struct.ObjectSet;
import mindustry.type.Liquid;
import net.liplum.CalledBySync;
import net.liplum.ClientOnly;
import net.liplum.lib.delegates.Delegate1;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IStreamClient extends IStreamNode {
    void readStream(@NotNull IStreamHost host, @NotNull Liquid liquid, float amount);

    /**
     * Gets the max acceptable number of this {@code liquid}.
     * negative number means any
     *
     * @param host   host
     * @param liquid liquid
     * @return amount
     */
    float acceptedAmount(@NotNull IStreamHost host, @NotNull Liquid liquid);

    @NotNull
    Delegate1<IStreamClient> getOnRequirementUpdated();

    @Nullable
    Liquid[] getRequirements();

    @CalledBySync
    default void connect(@NotNull IStreamHost host) {
        getConnectedHosts().add(host.getBuilding().pos());
    }

    @CalledBySync
    default void disconnect(@NotNull IStreamHost host) {
        getConnectedHosts().remove(host.getBuilding().pos());
    }

    @NotNull
    ObjectSet<Integer> getConnectedHosts();

    default boolean isConnectedWith(@NotNull IStreamHost host) {
        return getConnectedHosts().contains(host.getBuilding().pos());
    }

    /**
     * Gets the maximum limit of connection.<br/>
     * -1 : unlimited
     *
     * @return the maximum of connection
     */
    int maxHostConnection();

    default boolean acceptConnection(@NotNull IStreamHost host) {
        return canHaveMoreHostConnection();
    }

    default boolean canHaveMoreHostConnection() {
        int max = maxHostConnection();
        if (max == -1) {
            return true;
        }
        return getConnectedHosts().size < max;
    }

    default int getHostConnectionNumber() {
        return getConnectedHosts().size;
    }

    @NotNull
    @ClientOnly
    Color getClientColor();
}
