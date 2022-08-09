package net.liplum.holo

import mindustry.entities.units.WeaponMount
import mindustry.gen.Unit
import mindustry.type.Weapon

open class HoloWeapon : Weapon {
    @JvmField var shootConsumeLife = false
    var shootCost = 10f
        set(value) {
            field = value
            if (value > 0)
                shootConsumeLife = true
        }

    constructor(name: String) : super(name)
    constructor() : super()

    override fun shoot(unit: Unit, mount: WeaponMount, shootX: Float, shootY: Float, rotation: Float) {
        super.shoot(unit, mount, shootX, shootY, rotation)
        if (shootConsumeLife) {
            if (unit is HoloUnit)
                unit.time += shootCost
        }
    }
}