package net.liplum.registries

import mindustry.content.StatusEffects
import mindustry.type.Liquid
import net.liplum.R
import net.liplum.annotations.DependOn

object CioLiquids {
    @JvmStatic lateinit var cyberion: Liquid
    @JvmStatic lateinit var tissueFluid: Liquid
    @DependOn
    fun cyberion() {
        cyberion = Liquid(R.Liquid.Cyberion, R.C.Holo).apply {
            flammability = 0f
            explosiveness = 0f
            temperature = 0.1f
            heatCapacity = 1.4f
            viscosity = 0.8f
            boilPoint = 0.7f
            effect = StatusEffects.freezing
            lightColor = R.C.Holo.cpy().a(0.2f)
        }
    }
    @DependOn
    fun tissueFluid() {
        tissueFluid = Liquid(R.Liquid.TissueFluid, R.C.TissueFluid).apply {
            flammability = 0f
            explosiveness = 0f
            temperature = 0.56f // room temp = 0.5f
            heatCapacity = 0.3f
            viscosity = 0.6f
            boilPoint = 0.51f
            effect = StatusEffects.freezing
            lightColor = R.C.Holo.cpy().a(0.2f)
        }
    }
}