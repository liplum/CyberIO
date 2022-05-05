package net.liplum.blocks.prism

import mindustry.entities.bullet.BulletType
import mindustry.gen.Bullet
import mindustry.type.UnitType

enum class PrismData {
    Duplicate
}

val Any?.isDuplicate: Boolean
    get() = this == PrismData.Duplicate

fun Bullet.setDuplicate() {
    when (this.type) {
        else -> this.data = PrismData.Duplicate
    }
}

val PrismBlockList: HashSet<BulletType> = HashSet()
val PrismClzNameBlockList: HashSet<String> = HashSet()
val BulletType.canDisperse: Boolean
    get() = this !in PrismBlockList && this.javaClass.name !in PrismClzNameBlockList

fun String.banNameInPrism() {
    PrismClzNameBlockList.add(this)
}
fun BulletType.banInPrism() {
    PrismBlockList.add(this)
}

fun UnitType.banInWeapon(weaponName: String) {
    this.weapons.find {
        it.name == weaponName
    }?.bullet?.banInPrism()
}

fun UnitType.banInWeapons(vararg weaponNames: String) {
    for (name in weaponNames) {
        this.banInWeapon(name)
    }
}

fun UnitType.banAllInWeapons() {
    for (weapon in this.weapons) {
        weapon.bullet?.banInPrism()
    }
}