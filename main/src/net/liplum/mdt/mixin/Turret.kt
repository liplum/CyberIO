@file:Suppress("UNCHECKED_CAST")

package net.liplum.mdt.mixin

import mindustry.entities.pattern.ShootPattern
import mindustry.gen.Bullet
import mindustry.world.blocks.defense.turrets.Turret
import net.liplum.mdt.utils.draw

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

fun Bullet.copy(): Bullet {
    val d = Bullet.create()
    d.type = this.type
    d.owner = this.owner
    d.team = this.team
    d.x = this.x
    d.y = this.y
    d.aimX = this.aimX
    d.aimY = this.aimY
    d.aimTile = this.aimTile
    d.lifetime = this.lifetime
    d.time = this.time
    d.data = this.data
    d.drag = this.drag
    d.hitSize = this.hitSize
    d.damage = this.damage
    d.mover = this.mover
    d.fdata = this.fdata
    d.keepAlive = this.keepAlive
    d.originX = this.originX
    d.originY = this.originY
    d.lastX = this.lastX
    d.lastY = this.lastY
    d.hit = this.hit
    d.deltaX = this.deltaX
    d.deltaY = this.deltaY
    d.absorbed = this.absorbed
    //reset trail
    if (d.trail != null) {
        d.trail.clear()
    }
    d.vel = this.vel.cpy()
    d.add()
    return d
}