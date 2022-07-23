package net.liplum.registries

import mindustry.content.StatusEffects
import mindustry.type.Liquid
import net.liplum.*
import net.liplum.annotations.DependOn

object CioLiquids {
    @JvmStatic lateinit var cyberion: Liquid
    @JvmStatic lateinit var tissueFluid: Liquid
    @DependOn
    fun cyberion() {
        cyberion = Liquid(R.Liquid.Cyberion, S.Hologram).apply {
            VanillaSpec {
                flammability = 0f
                explosiveness = 0f
                temperature = 0.1f
                boilPoint = 0.7f
            }
            ErekirSpec {
                flammability = 0.8f
                explosiveness = 0.5f
                temperature = 1.5f
                boilPoint = 3f
            }
            heatCapacity = 1.4f
            viscosity = 0.8f
            effect = StatusEffects.freezing
            lightColor = S.Hologram.cpy().a(0.2f)
        }
    }
    @DependOn
    fun tissueFluid() {
        DebugOnly {
            tissueFluid = Liquid(R.Liquid.TissueFluid, R.C.TissueFluid).apply {
                flammability = 0f
                explosiveness = 0f
                temperature = 0.56f // room temp = 0.5f
                heatCapacity = 0.3f
                viscosity = 0.6f
                boilPoint = 0.51f
                effect = StatusEffects.freezing
                lightColor = S.Hologram.cpy().a(0.2f)
            }
        }
    }
}