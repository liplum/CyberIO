package net.liplum.registry

import mindustry.content.Fx
import mindustry.entities.bullet.BasicBulletType
import mindustry.graphics.Pal
import net.liplum.bullet.ShaderBasicBulletT
import net.liplum.common.shader.ShaderBase
import net.liplum.statusFx.StaticFx

object CioBulletType {
    fun BasicBulletType.optInVirus() {
        backColor = Pal.spore
        frontColor = Pal.spore
        despawnEffect = Fx.sporeSlowed
        hitEffect = Fx.sporeSlowed
        shootEffect = Fx.sporeSlowed
        smokeEffect = Fx.sporeSlowed
        status = CioSEffect.infected
        statusDuration = CioSEffect.infected.initTime
    }

    fun ShaderBasicBulletT<ShaderBase>.optInRadiationInterference() {
        shader = { SD.TvStatic }
        despawnEffect = StaticFx
        hitEffect = StaticFx
        shootEffect = StaticFx
        smokeEffect = StaticFx
        status = CioSEffect.static
        statusDuration = CioSEffect.static.initTime
    }
}