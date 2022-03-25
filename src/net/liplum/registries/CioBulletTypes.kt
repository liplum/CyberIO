package net.liplum.registries

import mindustry.content.Fx
import mindustry.entities.bullet.BasicBulletType
import mindustry.graphics.Pal
import net.liplum.ClientOnly
import net.liplum.R
import net.liplum.bullets.RuvikBullet
import net.liplum.bullets.STEM_VERSION
import net.liplum.bullets.ShaderBasicBulletT
import net.liplum.lib.shaders.SD
import net.liplum.lib.shaders.TrShader
import net.liplum.seffects.StaticFx
import net.liplum.shaders.holo.Hologram2

object CioBulletTypes : ContentTable {
    @JvmStatic lateinit var virus: BasicBulletType
    @JvmStatic lateinit var radiationInterference: ShaderBasicBulletT<TrShader>
    @JvmStatic lateinit var holoBullet: ShaderBasicBulletT<Hologram2>
    @JvmStatic lateinit var ruvik: RuvikBullet
    @JvmStatic lateinit var ruvik2: RuvikBullet
    override fun firstLoad() {
        holoBullet = ShaderBasicBulletT<Hologram2>(
            2f, 35f
        ).apply {
            ClientOnly {
                shader = SD.Hologram2
            }
            width = 10f
            height = 10f
            hitSize = 10f
            lifetime = 60f
            pierce = true
            pierceCap = 5
            backColor = R.C.HoloDark2
            frontColor = R.C.HoloDark2
        }
        ruvik = RuvikBullet(1.5f, 30f).apply {
            stemVersion = STEM_VERSION.STEM1
            width = 10f
            height = 10f
            hitSize = 10f
            lifetime = 240f
        }
        ruvik2 = RuvikBullet(1.5f, 30f).apply {
            stemVersion = STEM_VERSION.STEM2
            width = 10f
            height = 10f
            hitSize = 10f
            lifetime = 240f
        }
    }

    override fun load() {
        virus = BasicBulletType(
            2.5f, 45f, "bullet"
        ).apply {
            width = 10f
            height = 12f
            shrinkY = 0.1f
            lifetime = 100f
            hitSize = 10f
            backColor = Pal.spore
            frontColor = Pal.spore
            despawnEffect = Fx.sporeSlowed
            hitEffect = Fx.sporeSlowed
            shootEffect = Fx.sporeSlowed
            smokeEffect = Fx.sporeSlowed
            status = CioSEffects.infected
            statusDuration = CioSEffects.infected.initTime
        }

        radiationInterference = ShaderBasicBulletT<TrShader>(
            2.3f, 28f, "bullet"
        ).apply {
            ClientOnly {
                shader = SD.TvStatic
            }
            width = 15f
            height = 15f
            hitSize = 15f
            lifetime = 80f
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