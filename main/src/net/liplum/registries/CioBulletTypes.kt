package net.liplum.registries

import mindustry.content.Fx
import mindustry.entities.bullet.BasicBulletType
import mindustry.graphics.Pal
import net.liplum.ErekirSpec
import net.liplum.S
import net.liplum.VanillaSpec
import net.liplum.annotations.DependOn
import net.liplum.bullets.RuvikBullet
import net.liplum.bullets.STEM_VERSION
import net.liplum.bullets.ShaderBasicBulletT
import net.liplum.lib.shaders.SD
import net.liplum.mdt.shaders.CommonShader
import net.liplum.seffects.StaticFx
import net.liplum.shaders.HologramShader

object CioBulletTypes {
    @JvmStatic lateinit var virus: BasicBulletType
    @JvmStatic lateinit var radiationInterference: ShaderBasicBulletT<CommonShader>
    @JvmStatic lateinit var holoBullet: ShaderBasicBulletT<HologramShader>
    @JvmStatic lateinit var ruvik: RuvikBullet
    @JvmStatic lateinit var ruvik2: RuvikBullet
    @DependOn
    fun holoBullet() {
        holoBullet = ShaderBasicBulletT<HologramShader>(
            2f, 35f
        ).apply {
            shader = { SD.Hologram }
            width = 10f
            height = 10f
            hitSize = 10f
            lifetime = 60f
            pierce = true
            pierceCap = 5
            backColor = S.HologramDark
            frontColor = S.HologramDark
        }
    }
    @DependOn
    fun ruvik() {
        ruvik = RuvikBullet(1.5f, 30f).apply {
            stemVersion = STEM_VERSION.STEM1
            width = 10f
            height = 10f
            hitSize = 10f
            lifetime = 240f
        }
    }
    @DependOn
    fun ruvik2() {
        ruvik2 = RuvikBullet(1.5f, 30f).apply {
            stemVersion = STEM_VERSION.STEM2
            width = 10f
            height = 10f
            hitSize = 10f
            lifetime = 240f
        }
    }
    @DependOn("CioSEffects.infected")
    fun virus() {
        virus = BasicBulletType().apply {
            VanillaSpec {
                speed = 2.5f
                damage = 50f
            }
            //TODO: Create a new bullet instead?
            ErekirSpec {
                speed = 2.8f
                damage = 80f
            }
            sprite = "bullet"
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
    }
    @DependOn(
        "CioSEffects.static"
    )
    fun radiationInterference() {
        radiationInterference = ShaderBasicBulletT<CommonShader>().apply {
            VanillaSpec {
                speed = 2.3f
                damage = 30f
            }
            ErekirSpec {
                speed = 2.25f
                damage = 90f
            }
            sprite = "bullet"
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
}