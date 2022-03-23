package net.liplum.api.cyber;

import arc.graphics.Color;
import arc.struct.OrderedSet;
import mindustry.type.Liquid;
import net.liplum.ClientOnly;
import net.liplum.SendDataPack;
import org.jetbrains.annotations.NotNull;

public interface IStreamHost extends IStreamNode {
    /**
     * sends liquid
     *
     * @param client the target who receives the liquid
     * @param liquid which kind of liquid will be sent soon
     * @param amount how much liquid will be sent
     * @return the rest of liquid
     */
    default float streaming(@NotNull IStreamClient client, @NotNull Liquid liquid, float amount) {
        float maxAccepted = client.acceptedAmount(this, liquid);
        if (maxAccepted == -1) {
            client.readStream(this, liquid, amount);
            return 0;
        }
        if (maxAccepted >= amount) {
            client.readStream(this, liquid, amount);
            return 0;
        } else {
            float rest = amount - maxAccepted;
            client.readStream(this, liquid, maxAccepted);
            return rest;
        }
    }

    @SendDataPack
    void connectSync(@NotNull IStreamClient client);

    @SendDataPack
    void disconnectSync(@NotNull IStreamClient client);

    default boolean isConnectedWith(@NotNull IStreamClient client) {
        return connectedClients().contains(client.getBuilding().pos());
    }

    /**
     * Gets the maximum limit of connection.<br/>
     * -1 : unlimited
     *
     * @return the maximum of connection
     */
    int maxClientConnection();

    default boolean canHaveMoreClientConnection() {
        int max = maxClientConnection();
        if (max == -1) {
            return true;
        }
        return connectedClients().size < max;
    }

    @NotNull
    OrderedSet<Integer> connectedClients();

    @NotNull
    @ClientOnly
    Color getHostColor();
}
