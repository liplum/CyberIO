package net.liplum.blocks.gadgets

import net.liplum.api.cyber.IDataReceiver
import kotlin.math.absoluteValue

open class Tracker(val maxConnection: Int) {
    val receivers: ArrayList<IDataReceiver> = ArrayList(maxConnection)
    var curIndex = 0
        set(value) {
            field = (if (receivers.isEmpty())
                0
            else
                value.absoluteValue % receivers.size)
        }

    fun canAddMore(): Boolean = receivers.size <= maxConnection

    fun add(receiver: IDataReceiver) {
        if (canAddMore()) {
            receivers.add(receiver)
        }
    }

    fun clear() {
        receivers.clear()
        curIndex = 0
    }
}
