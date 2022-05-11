package net.liplum.registries

import mindustry.content.Fx
import mindustry.entities.bullet.BasicBulletType
import mindustry.graphics.Pal
import net.liplum.R
import net.liplum.bullets.RuvikBullet
import net.liplum.bullets.STEM_VERSION
import net.liplum.bullets.ShaderBasicBulletT
import net.liplum.lib.shaders.CommonShader
import net.liplum.lib.shaders.SD
import net.liplum.seffects.StaticFx
import net.liplum.shaders.holo.Hologram

object CioBulletTypes : ContentTable {
    @JvmStatic lateinit var virus: BasicBulletType
    @JvmStatic lateinit var radiationInterference: ShaderBasicBulletT<CommonShader>
    @JvmStatic lateinit var holoBullet: ShaderBasicBulletT<Hologram>
    @JvmStatic lateinit var ruvik: RuvikBullet
    @JvmStatic lateinit var ruvik2: RuvikBullet
    override fun firstLoad() {
        holoBullet = ShaderBasicBulletT<Hologram>(
            2f, 35f
        ).apply {
            shader = { SD.Hologram }
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
            2.5f, 50f, "bullet"
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

        radiationInterference = ShaderBasicBulletT<CommonShader>(
            2.3f, 30f, "bullet"
        ).apply {
            shader = { SD.TvStatic }
            width = 15f
            height = 15f
            hitSize = 15f
            lifetime = 80f
            despawnEffect = StaticFx
            hitEffect = StaticFx
            shootEffect = StaticFx
            smokeEffect = StaticFx
            pierce = true
            pierceCap = 2
            status = CioSEffects.static
            statusDuration = CioSEffects.static.initTime
        }
    }

    override fun lastLoad() {
    }
}