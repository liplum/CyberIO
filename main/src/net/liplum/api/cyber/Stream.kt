package net.liplum.api.cyber

import arc.graphics.Color
import arc.struct.ObjectSet
import arc.struct.OrderedSet
import arc.struct.Seq
import mindustry.type.Liquid
import net.liplum.api.ICyberEntity
import net.liplum.common.delegate.Delegate1
import net.liplum.utils.CalledBySync
import plumy.core.ClientOnly
import net.liplum.utils.SendDataPack

interface IStreamServer : IStreamHost, IStreamClient
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

interface IStreamClient : ICyberEntity {
    fun readStreamFrom(host: IStreamHost, liquid: Liquid, amount: Float)
    /**
     * Gets the max acceptable number of this `liquid`.
     * negative number means any
     *
     * @param host   host
     * @param liquid liquid
     * @return amount
     */
    fun getAcceptedAmount(host: IStreamHost, liquid: Liquid): Float
    val onRequirementUpdated: Delegate1<IStreamClient>
    /**
     * Gets what this client wants<br></br>
     * null : Any<br></br>
     * Array.Empty : Nothing<br></br>
     * An seq : all things in the array<br></br>
     * Please cache this value, this is a mutable list.
     * @return what this client wants
     */
    val requirements: Seq<Liquid>?
    @CalledBySync
    fun onConnectFrom(host: IStreamHost) {
        connectedHosts.add(host.building.pos())
    }
    @CalledBySync
    fun onDisconnectFrom(host: IStreamHost) {
        connectedHosts.remove(host.building.pos())
    }

    val connectedHosts: ObjectSet<Int>
    fun isConnectedTo(host: IStreamHost): Boolean {
        return connectedHosts.contains(host.building.pos())
    }
    /**
     * Gets the maximum limit of connection.<br></br>
     * -1 : unlimited
     *
     * @return the maximum of connection
     */
    val maxHostConnection: Int
    fun acceptConnectionTo(host: IStreamHost) = canHaveMoreHostConnection
    val canHaveMoreHostConnection: Boolean
        get() = maxHostConnection == -1 || hostConnectionNumber < maxHostConnection
    val hostConnectionNumber: Int
        get() = connectedHosts.size
    @ClientOnly
    val clientColor: Color
}