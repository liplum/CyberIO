package net.liplum.api.cyber

import arc.graphics.Color
import arc.struct.ObjectSet
import mindustry.type.Item
import net.liplum.R
import net.liplum.api.ICyberEntity
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.SendDataPack

interface IDataSender : ICyberEntity {
    /**
     * sends items
     *
     * @param receiver the target who receives the item(s)
     * @param item     which kind of item will be sent soon
     * @param amount   how many item(s) will be sent
     * @return the rest of item(s)
     */
    fun sendData(receiver: IDataReceiver, item: Item, amount: Int): Int {
        val maxAccepted = receiver.acceptedAmount(this, item)
        if (maxAccepted == -1) {
            receiver.receiveData(this, item, amount)
            return 0
        }
        return if (maxAccepted >= amount) {
            receiver.receiveData(this, item, amount)
            0
        } else {
            val rest = amount - maxAccepted
            receiver.receiveData(this, item, maxAccepted)
            rest
        }
    }
    @SendDataPack
    fun connectSync(receiver: IDataReceiver)
    @SendDataPack
    fun disconnectSync(receiver: IDataReceiver)
    @SendDataPack
    fun connectSync(receiver: Int) {
        val dr = receiver.dr()
        dr?.let { connectSync(it) }
    }
    @SendDataPack
    fun disconnectSync(receiver: Int) {
        val dr = receiver.dr()
        dr?.let { disconnectSync(it) }
    }

    val connectedReceiver: Int?
    val canMultipleConnect: Boolean
        get() = maxReceiverConnection != 1

    fun isConnectedWith(receiver: IDataReceiver): Boolean {
        return if (canMultipleConnect) {
            connectedReceivers.contains(receiver.building.pos())
        } else {
            val connected = connectedReceiver
            if (connected == null) {
                false
            } else {
                connected == receiver.building.pos()
            }
        }
    }
    /**
     * Gets the maximum limit of connection.<br></br>
     * -1 : unlimited
     *
     * @return the maximum of connection
     */
    val maxReceiverConnection: Int
        get() = 1

    fun canHaveMoreReceiverConnection(): Boolean {
        val max = maxReceiverConnection
        return if (max == -1) {
            true
        } else connectedReceivers.size < max
    }

    val receiverConnectionNumber: Int
        get() = if (canMultipleConnect) connectedReceivers.size
        else if (connectedReceiver == null) 0
        else 1
    @ClientOnly
    val senderColor: Color
        get() = R.C.Sender
    val maxRange: Float
        get() = -1f
    val connectedReceivers: ObjectSet<Int>
        get() = emptyConnection

    companion object {
        /**
         * Only for single connection
         */
        val emptyConnection = ObjectSet<Int>()
    }
}