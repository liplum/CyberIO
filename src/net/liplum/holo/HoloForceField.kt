package net.liplum.holo

import mindustry.entities.abilities.ForceFieldAbility
import mindustry.gen.Unit
import mindustry.graphics.Layer
import net.liplum.abilites.localized
import net.liplum.shaders.SD
import net.liplum.shaders.use

open class HoloForceField(
    radius: Float, regen: Float, max: Float, cooldown: Float
) : ForceFieldAbility(radius, regen, max, cooldown) {
    override fun draw(unit: Unit) {
        SD.hologram2.use(Layer.shields) {
            super.draw(unit)
        }
    }

    override fun localized() =
        this.javaClass.localized()
}