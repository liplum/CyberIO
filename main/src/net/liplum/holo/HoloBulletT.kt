package net.liplum.holo

import mindustry.entities.bullet.BasicBulletType

open class HoloBulletT : BasicBulletType {
    constructor(speed: Float, damage: Float, bulletSprite: String)
            : super(speed, damage, bulletSprite)

    constructor(speed: Float, damage: Float) :
            this(speed, damage, "bullet")

    constructor() : this(1f, 1f, "bullet")
}