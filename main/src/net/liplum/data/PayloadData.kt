package net.liplum.data

import mindustry.world.blocks.payloads.Payload

class PayloadData {
    var data: Payload? = null
    val isEmpty: Boolean
        get() = data == null

    fun swap(other: PayloadData) {
        val tmp = data
        data = other.data
        other.data = tmp
    }
}