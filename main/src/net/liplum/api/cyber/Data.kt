package net.liplum.api.cyber

import arc.graphics.Color
import arc.struct.ObjectSet
import arc.struct.Seq
import mindustry.type.Item
import net.liplum.R
import net.liplum.api.ICyberEntity
import net.liplum.common.delegate.Delegate1
import net.liplum.utils.CalledBySync
import plumy.core.ClientOnly
import net.liplum.utils.SendDataPack

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
    fun isConnectedTo(receiver: IDataReceiver): Boolean {
        return receiver.building.pos() in connectedReceivers
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
        get() = connectedReceivers.size
    @ClientOnly
    val senderColor: Color
        get() = R.C.Sender
    val maxRange: Float
        get() = -1f
    val connectedReceivers: ObjectSet<Int>

    companion object {
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

interface IDataReceiver : ICyberEntity {
    fun receiveDataFrom(sender: IDataSender, item: Item, amount: Int)
    /**
     * Gets the max acceptable number of this `item`.
     * -1 means any
     *
     * @param sender sender
     * @param item   item
     * @return amount
     */
    fun getAcceptedAmount(sender: IDataSender, item: Item): Int
    /**
     * Gets what this receiver wants<br></br>
     * null : Any<br></br>
     * Array.Empty : Nothing<br></br>
     * An seq : all things in the array<br></br>
     * Please cache this value, this is a mutable list.
     * @return what this receiver wants
     */
    val requirements: Seq<Item>?
    @ClientOnly
    val receiverColor: Color
        get() = R.C.Receiver
    @CalledBySync
    fun onConnectTo(sender: IDataSender) {
        connectedSenders.add(sender.building.pos())
    }
    @CalledBySync
    fun onDisconnectFrom(sender: IDataSender) {
        connectedSenders.remove(sender.building.pos())
    }

    val connectedSenders: ObjectSet<Int>
    fun isConnectedTo(sender: IDataSender): Boolean {
        return connectedSenders.contains(sender.building.pos())
    }

    val onRequirementUpdated: Delegate1<IDataReceiver>
    /**
     * Gets the maximum limit of connection.<br></br>
     * -1 : unlimited
     *
     * @return the maximum of connection
     */
    val maxSenderConnection: Int
    fun acceptConnectionTo(sender: IDataSender) = canHaveMoreSenderConnection
    val senderConnectionNumber: Int
        get() = connectedSenders.size
    val canHaveMoreSenderConnection: Boolean
        get() = maxSenderConnection == -1 || senderConnectionNumber < maxSenderConnection
}