package net.liplum.blocks.gadgets

import net.liplum.api.data.IDataReceiver

open class Tracker(val maxConnection: Int) {
    val receivers: ArrayList<IDataReceiver> = ArrayList(maxConnection)
    fun canAddMore(): Boolean {
        return receivers.size <= maxConnection
    }

    fun add(receiver: IDataReceiver) {
        if (canAddMore()) {
            receivers.add(receiver)
        }
    }

    fun clear() {
        receivers.clear()
    }
}
