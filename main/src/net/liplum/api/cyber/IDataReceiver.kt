package net.liplum.api.cyber

import arc.graphics.Color
import arc.struct.ObjectSet
import arc.struct.Seq
import mindustry.type.Item
import net.liplum.R
import net.liplum.api.ICyberEntity
import net.liplum.common.Changed
import net.liplum.common.delegates.Delegate1
import net.liplum.mdt.CalledBySync
import net.liplum.mdt.ClientOnly

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
    fun connectTo(sender: IDataSender) {
        connectedSenders.add(sender.building.pos())
    }
    @CalledBySync
    fun disconnectFrom(sender: IDataSender) {
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