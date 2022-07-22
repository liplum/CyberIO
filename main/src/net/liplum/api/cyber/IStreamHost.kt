package net.liplum.api.cyber

import arc.graphics.Color
import arc.struct.ObjectSet
import arc.struct.OrderedSet
import mindustry.type.Liquid
import net.liplum.api.ICyberEntity
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.SendDataPack

interface IStreamHost : ICyberEntity {
    /**
     * sends liquid
     *
     * @param client the target who receives the liquid
     * @param liquid which kind of liquid will be sent soon
     * @param amount how much liquid will be sent
     * @return the rest of liquid
     */
    fun streamTo(client: IStreamClient, liquid: Liquid, amount: Float): Float {
        val maxAccepted = client.getAcceptedAmount(this, liquid)
        if (maxAccepted < 0) {
            client.readStreamFrom(this, liquid, amount)
            return 0f
        }
        return if (maxAccepted >= amount) {
            client.readStreamFrom(this, liquid, amount)
            0f
        } else {
            val rest = amount - maxAccepted
            client.readStreamFrom(this, liquid, maxAccepted)
            rest
        }
    }
    @SendDataPack
    fun connectToSync(client: IStreamClient)
    @SendDataPack
    fun disconnectFromSync(client: IStreamClient)
    fun isConnectedTo(client: IStreamClient): Boolean {
        return connectedClients.contains(client.building.pos())
    }
    /**
     * Gets the maximum limit of connection.<br></br>
     * -1 : unlimited
     *
     * @return the maximum of connection
     */
    val maxClientConnection: Int
    val canHaveMoreClientConnection: Boolean
        get() = maxClientConnection == -1 || clientConnectionNumber < maxClientConnection
    val clientConnectionNumber: Int
        get() = connectedClients.size
    val connectedClients: OrderedSet<Int>
    @ClientOnly
    val hostColor: Color
    val maxRange: Float
        get() = -1f

    companion object {
        /**
         * Only for single connection
         */
        val emptyConnection = ObjectSet<Int>()
        @SendDataPack
        fun IStreamHost.connectToSync(client: Int) {
            client.sc()?.let { connectToSync(it) }
        }
        @SendDataPack
        fun IStreamHost.disconnectFromSync(client: Int) {
            client.sc()?.let { disconnectFromSync(it) }
        }
    }
}