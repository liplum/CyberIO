package net.liplum.data

import arc.graphics.Color
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
    fun genMixedColor(): Color? = when (receivers.size) {
        0 -> null
        1 -> receivers[0].receiverColor
        else -> {
            val c = Color.gray.cpy()
            for (receiver in receivers) {
                c.lerp(receiver.receiverColor, 1f / receivers.size)
            }
            c
        }
    }

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
