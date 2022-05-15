package net.liplum.registries

import mindustry.content.StatusEffects
import mindustry.type.Liquid
import net.liplum.R

object CioLiquids : ContentTable {
    @JvmStatic
    lateinit var cyberion: Liquid
    override fun firstLoad() {
        cyberion = Liquid(R.Liquid.Cyberion, R.C.Holo).apply {
            flammability = 0f
            explosiveness = 0f
            temperature = 0.1f
            heatCapacity = 1.4f
            viscosity = 0.8f
            effect = StatusEffects.freezing
            lightColor = R.C.Holo.cpy().a(0.2f)
        }
    }

    override fun load() {
    }

    override fun lastLoad() {
    }
}