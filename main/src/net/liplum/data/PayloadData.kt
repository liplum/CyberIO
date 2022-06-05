package net.liplum.data

import arc.struct.Seq
import mindustry.world.blocks.payloads.Payload
import net.liplum.lib.Serialized

class PayloadDataList(
    var capacity: Int = 1,
) : Iterable<PayloadData> {
    @Serialized
    var allData = Seq<PayloadData>()
    val isEmpty: Boolean
        get() = allData.isEmpty
    val canAddMore: Boolean
        get() = allData.size < capacity
    val size: Int
        get() = allData.size

    operator fun get(index: Int): PayloadData =
        allData[index]

    fun add(payload: PayloadData): Boolean {
        if (allData.size < capacity) {
            allData.add(payload)
            return true
        }
        return false
    }

    fun remove(payload: PayloadData): Boolean =
        allData.remove(payload)

    fun first(): PayloadData = allData.first()
    override fun iterator(): Iterator<PayloadData> =
        allData.iterator()

    fun indexPayload(payload: PayloadData): Int =
        allData.indexOf(payload, true)
}
data class PayloadData(
    @Serialized
    var payload: Payload,
    @Serialized
    var id: Int,
)

val PayloadDataList.isNotEmpty: Boolean
    get() = !isEmpty