package net.liplum.registry

import mindustry.content.Fx
import mindustry.entities.bullet.BasicBulletType
import mindustry.graphics.Pal
import net.liplum.ErekirSpec
import net.liplum.VanillaSpec
import net.liplum.annotations.DependOn
import net.liplum.bullet.ShaderBasicBulletT
import net.liplum.mdt.shader.CommonShader
import net.liplum.statusFx.StaticFx

object CioBulletTypes {
    @JvmStatic lateinit var virus: BasicBulletType
    @JvmStatic lateinit var radiationInterference: ShaderBasicBulletT<CommonShader>

    @DependOn("CioSEffects.infected")
    fun virus() {
        virus = BasicBulletType().apply {
            VanillaSpec {
                speed = 2.5f
                damage = 50f
            }
            ErekirSpec {
                speed = 2.8f
                damage = 180f
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
                damage = 40f
            }
            ErekirSpec {
                speed = 2.25f
                damage = 280f
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