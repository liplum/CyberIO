package net.liplum.registries

import mindustry.content.Fx
import mindustry.entities.bullet.BasicBulletType
import mindustry.entities.bullet.BulletType
import mindustry.graphics.Pal
import net.liplum.ClientOnly
import net.liplum.bullets.ShaderBasicBulletT
import net.liplum.seffects.StaticFx
import net.liplum.shaders.SD

object CioBulletTypes : ContentTable {
    @JvmStatic lateinit var virus: BulletType
    @JvmStatic lateinit var radiationInterference: BulletType
    override fun firstLoad() {
    }

    override fun load() {
        virus = BasicBulletType(2.5f, 45f, "bullet").apply {
            width = 10f
            height = 12f
            shrinkY = 0.1f
            lifetime = 100f
            backColor = Pal.spore
            frontColor = Pal.spore
            despawnEffect = Fx.sporeSlowed
            hitEffect = Fx.sporeSlowed
            shootEffect = Fx.sporeSlowed
            smokeEffect = Fx.sporeSlowed
            status = CioSEffects.infected
            statusDuration = CioSEffects.infected.initTime
        }

        radiationInterference = ShaderBasicBulletT(1.0f, 28f, "bullet").apply {
            ClientOnly {
                shader = SD.tvSnow
            }
            width = 10f
            height = 12f
            lifetime = 100f
            despawnEffect = StaticFx
            hitEffect = StaticFx
            shootEffect = StaticFx
            smokeEffect = StaticFx
            status = CioSEffects.static
            statusDuration = CioSEffects.static.initTime
        }
    }

    override fun lastLoad() {
    }
}