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

val PrismDispersionBlockList: HashSet<BulletType> = HashSet()
val BulletType.canDispersion: Boolean
    get() = this !in PrismDispersionBlockList

fun BulletType.banInPrism() {
    PrismDispersionBlockList.add(this)
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