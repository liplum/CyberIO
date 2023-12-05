package net.liplum.registry

import mindustry.content.StatusEffects
import mindustry.type.Liquid
import net.liplum.*
import net.liplum.annotations.DependOn
import net.liplum.type.SpecFluid

object CioFluid {
    @JvmStatic lateinit var cyberion: Liquid
    @JvmStatic lateinit var blood: Liquid
    @DependOn
    fun cyberion() {
        cyberion = SpecFluid(R.Liquid.Cyberion, Var.Hologram).apply {
            VanillaSpec {
                flammability = 0f
                explosiveness = 0f
                temperature = 0f
                boilPoint = 0.7f
                effect = StatusEffects.freezing
            }
            ErekirSpec {
                flammability = 0.3f
                explosiveness = 0.5f
                temperature = 1.8f
                boilPoint = 3f
                effect = StatusEffects.melting
            }
            heatCapacity = 1.4f
            viscosity = 0.8f
            lightColor = Var.Hologram.cpy().a(0.2f)
        }
    }
    @DependOn
    fun blood() {
        DebugOnly {
            blood = Liquid(R.Liquid.Blood, R.C.Blood).apply {
                flammability = 0f
                explosiveness = 0f
                temperature = 0.56f // room temp = 0.5f
                heatCapacity = 0.3f
                viscosity = 0.6f
                boilPoint = 0.51f
                lightColor = Var.Hologram.cpy().a(0.2f)
            }
        }
    }
}