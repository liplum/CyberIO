package net.liplum.utils

import arc.func.Prov
import mindustry.Vars
import mindustry.gen.EntityMapping

fun registerUnitType(name: String, Number: Int = 3) {
    EntityMapping.nameMap.put(
        Vars.content.transformName(name),
        EntityMapping.idMap[Number]
    )
}

fun registerUnitType(name: String, constructor: Prov<*>) {
    EntityMapping.nameMap.put(
        Vars.content.transformName(name),
        constructor
    )
}
fun <T> NewUnitType(name: String, typeCtor: (String) -> T, Number: Int = 3): T {
    EntityMapping.nameMap.put(
        Vars.content.transformName(name),
        EntityMapping.idMap[Number]
    )
    return typeCtor(name)
}

inline fun <T> NewUnitType(
    name: String, noinline typeCtor: (String) -> T, unitCtor: Prov<*>,
    config: T.()->Unit
): T {
    EntityMapping.nameMap.put(
        Vars.content.transformName(name),
        unitCtor
    )
    return typeCtor(name).apply(config)
}
