package net.liplum.api.cyber

import arc.graphics.Color
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
    fun streaming(client: IStreamClient, liquid: Liquid, amount: Float): Float {
        val maxAccepted = client.acceptedAmount(this, liquid)
        if (maxAccepted < 0) {
            client.readStream(this, liquid, amount)
            return 0f
        }
        return if (maxAccepted >= amount) {
            client.readStream(this, liquid, amount)
            0f
        } else {
            val rest = amount - maxAccepted
            client.readStream(this, liquid, maxAccepted)
            rest
        }
    }
    @SendDataPack
    fun connectSync(client: IStreamClient)
    @SendDataPack
    fun disconnectSync(client: IStreamClient)
    fun isConnectedWith(client: IStreamClient): Boolean {
        return connectedClients.contains(client.building.pos())
    }
    /**
     * Gets the maximum limit of connection.<br></br>
     * -1 : unlimited
     *
     * @return the maximum of connection
     */
    val maxClientConnection: Int
    fun canHaveMoreClientConnection(): Boolean {
        val max = maxClientConnection
        return if (max == -1) {
            true
        } else connectedClients.size < max
    }

    val clientConnectionNumber: Int
        get() = connectedClients.size
    val connectedClients: OrderedSet<Int>
    @ClientOnly
    val hostColor: Color
    val maxRange: Float
        get() = -1f
}