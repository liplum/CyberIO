package net.liplum.utils

import mindustry.gen.Bullet

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
    //reset trail
    if (d.trail != null) {
        d.trail.clear()
    }
    d.vel = this.vel.cpy()
    d.add()
    return d
}