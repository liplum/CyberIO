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
    fun sendDataTo(receiver: IDataReceiver, item: Item, amount: Int): Int {
        val maxAccepted = receiver.getAcceptedAmount(this, item)
        if (maxAccepted == -1) {
            receiver.receiveDataFrom(this, item, amount)
            return 0
        }
        return if (maxAccepted >= amount) {
            receiver.receiveDataFrom(this, item, amount)
            0
        } else {
            val rest = amount - maxAccepted
            receiver.receiveDataFrom(this, item, maxAccepted)
            rest
        }
    }
    @SendDataPack
    fun connectToSync(receiver: IDataReceiver)
    @SendDataPack
    fun disconnectFromSync(receiver: IDataReceiver)
    val connectedReceiver: Int?
    val canMultipleConnect: Boolean
        get() = maxReceiverConnection != 1

    fun isConnectedTo(receiver: IDataReceiver): Boolean {
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
    val canHaveMoreReceiverConnection: Boolean
        get() = maxReceiverConnection == -1 || receiverConnectionNumber < maxReceiverConnection
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
        @SendDataPack
        fun IDataSender.connectToSync(receiver: Int) {
            receiver.dr()?.let { connectToSync(it) }
        }
        @SendDataPack
        fun IDataSender.disconnectFromSync(receiver: Int) {
            receiver.dr()?.let { disconnectFromSync(it) }
        }
    }
}