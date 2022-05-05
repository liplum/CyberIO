package net.liplum.holo

import mindustry.entities.units.WeaponMount
import mindustry.gen.Unit
import mindustry.type.Weapon
import net.liplum.utils.findLeaderInFormation

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

    override fun shoot(
        unit: Unit,
        mount: WeaponMount,
        shootX: Float,
        shootY: Float,
        aimX: Float,
        aimY: Float,
        mountX: Float,
        mountY: Float,
        rotation: Float,
        side: Int
    ) {
        val leader = unit.findLeaderInFormation()
        super.shoot(leader, mount, shootX, shootY, aimX, aimY, mountX, mountY, rotation, side)
        if (shootConsumeLife && unit is HoloUnit) {
            unit.time += shootCost
        }
    }
}