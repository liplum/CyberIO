@file:Suppress("UNCHECKED_CAST")

package net.liplum.lib.mixin

import mindustry.entities.pattern.ShootPattern
import mindustry.world.blocks.defense.turrets.Turret
import net.liplum.draw

fun <T : ShootPattern> Turret.shootPattern(): T {
    return this.shoot as T
}

fun <T : ShootPattern> Turret.shootPattern(
    init: T,
): T {
    this.shoot = init
    return init
}

val Turret.TurretBuild.drawRotation: Float
    get() = rotation.draw
val Turret.TurretBuild.drawX: Float
    get() = x + recoilOffset.x
val Turret.TurretBuild.drawY: Float
    get() = y + recoilOffset.y