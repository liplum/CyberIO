package net.liplum.mdt.utils

import arc.func.Prov
import mindustry.Vars
import mindustry.gen.EntityMapping

@JvmOverloads
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
@JvmOverloads
fun <T> NewUnitType(name: String, typeCtor: (String) -> T, Number: Int = 3): T {
    EntityMapping.nameMap.put(
        Vars.content.transformName(name),
        EntityMapping.idMap[Number]
    )
    return typeCtor(name)
}

fun <T> NewUnitType(name: String, typeCtor: (String) -> T, unitCtor: Prov<*>): T {
    EntityMapping.nameMap.put(
        Vars.content.transformName(name),
        unitCtor
    )
    return typeCtor(name)
}
