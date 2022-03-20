package net.liplum.utils

import mindustry.Vars
import mindustry.gen.EntityMapping

@JvmOverloads
fun registerUnitType(name: String, Number: Int = 3) {
    EntityMapping.nameMap.put(
        Vars.content.transformName(name),
        EntityMapping.idMap[Number]
    )
}
@JvmOverloads
fun <T> NewUnitType(name: String, constructor: (String) -> T, Number: Int = 3): T {
    EntityMapping.nameMap.put(
        Vars.content.transformName(name),
        EntityMapping.idMap[Number]
    )
    return constructor(name)
}
