package net.liplum.registries

import mindustry.content.Fx
import mindustry.entities.bullet.BasicBulletType
import mindustry.entities.bullet.BulletType
import mindustry.graphics.Pal

class CioBulletTypes : ContentTable {
    companion object {
        @JvmStatic lateinit var virus: BulletType
    }

    override fun firstLoad() {
    }

    override fun load() {
        virus = BasicBulletType(2.5f, 5f, "bullet").apply {
            width = 10f
            height = 12f
            shrinkY = 0.1f
            lifetime = 70f
            backColor = Pal.spore
            frontColor = Pal.spore
            despawnEffect = Fx.sporeSlowed
            hitEffect = Fx.sporeSlowed
            shootEffect = Fx.sporeSlowed
            smokeEffect = Fx.sporeSlowed
            status = CioStatusEffects.infected
            statusDuration = CioStatusEffects.infected.infectedInitTime
        }
    }

    override fun lastLoad() {
    }
}