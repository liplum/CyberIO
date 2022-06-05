package net.liplum.data

import arc.struct.Seq
import mindustry.world.blocks.payloads.Payload

class PayloadDataList(
    var capacity: Int = 1
) : Iterable<Payload> {
    var allData = Seq<Payload>()
    val isEmpty: Boolean
        get() = allData.isEmpty
    val canAddMore: Boolean
        get() = allData.size < capacity

    operator fun get(index: Int): Payload =
        allData[index]

    fun add(payload: Payload) {
        allData.add(payload)
    }

    fun remove(payload: Payload) {
        allData.remove(payload)
    }

    fun first(): Payload = allData.first()
    override fun iterator(): Iterator<Payload> =
        allData.iterator()

    fun indexPayload(payload: Payload): Int =
        allData.indexOf(payload, true)
}

val PayloadDataList.isNotEmpty: Boolean
    get() = !isEmpty