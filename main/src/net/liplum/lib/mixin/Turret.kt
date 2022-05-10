@file:Suppress("UNCHECKED_CAST")

package net.liplum.lib.mixin

import mindustry.entities.pattern.ShootPattern
import mindustry.world.blocks.defense.turrets.Turret

fun <T : ShootPattern> Turret.shootPattern(): T {
    return this.shoot as T
}

fun <T : ShootPattern> Turret.shootPattern(
    init: T,
): T {
    this.shoot = init
    return init
}
