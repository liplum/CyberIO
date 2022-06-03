package net.liplum.data

import mindustry.world.blocks.payloads.Payload

class PayloadDataList(
    var capacity: Int = 1
) {
    var allData = ArrayList<Payload>()
    val isEmpty: Boolean
        get() = allData.isEmpty()
    val canAddMore :Boolean
        get() = allData.size < capacity
}

val PayloadDataList.isNotEmpty: Boolean
    get() = !isEmpty