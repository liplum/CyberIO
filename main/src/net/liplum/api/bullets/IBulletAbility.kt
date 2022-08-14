package net.liplum.api.bullets

import mindustry.content.Bullets
import mindustry.entities.bullet.BulletType
import mindustry.gen.Bullet

interface IBulletAbility {
    var bulletType: BulletType
    fun update(b: Bullet) {}
    fun draw(b: Bullet) {}
    fun init(b: Bullet) {}
    fun preInit(type: BulletType) {}
    fun postInit(type: BulletType) {}
    fun despawned(b: Bullet) {}
}

open class BulletAbility : IBulletAbility {
    override var bulletType: BulletType =
        Bullets.placeholder
}

object EmptyBulletAbility : BulletAbility()
class MultiBulletAbility(
    vararg val abilities: IBulletAbility,
) : BulletAbility() {
    override var bulletType: BulletType = Bullets.placeholder
        set(value) {
            field = value
            abilities.forEach { it.bulletType = value }
        }

    override fun init(b: Bullet) {
        abilities.forEach { it.init(b) }
    }

    override fun update(b: Bullet) {
        abilities.forEach { it.update(b) }
    }

    override fun draw(b: Bullet) {
        abilities.forEach { it.draw(b) }
    }

    override fun despawned(b: Bullet) {
        abilities.forEach { it.despawned(b) }
    }

    override fun preInit(type: BulletType) {
        abilities.forEach { it.preInit(type) }
    }

    override fun postInit(type: BulletType) {
        abilities.forEach { it.postInit(type) }
    }
}

