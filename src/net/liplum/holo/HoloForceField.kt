package net.liplum.holo

import arc.Core
import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Fill
import arc.graphics.g2d.Lines
import arc.math.Mathf
import mindustry.entities.abilities.ForceFieldAbility
import mindustry.gen.Unit
import mindustry.graphics.Layer
import net.liplum.Cptb
import net.liplum.R
import net.liplum.abilites.localized
import net.liplum.lib.shaders.SD
import net.liplum.lib.shaders.use

open class HoloForceField(
    radius: Float, regen: Float, max: Float, cooldown: Float
) : ForceFieldAbility(radius, regen, max, cooldown) {
    override fun draw(unit: Unit) {
        if (Cptb.HoloForceField)
            vanillaDraw(unit)
        else
            shaderDraw(unit)
    }

    open fun shaderDraw(unit: Unit) {
        SD.Hologram2.use(Layer.shields) {
            super.draw(unit)
        }
    }

    open fun vanillaDraw(unit: Unit) {
        val realRad = radiusScale * radius

        if (unit.shield > 0) {
            Draw.z(Layer.shields)
            Draw.color(R.C.Holo, Color.white, Mathf.clamp(alpha))
            if (Core.settings.getBool("animatedshields")) {
                Fill.poly(unit.x, unit.y, 6, realRad)
            } else {
                Lines.stroke(1.5f)
                Draw.alpha(0.09f)
                Fill.poly(unit.x, unit.y, 6, radius)
                Draw.alpha(1f)
                Lines.poly(unit.x, unit.y, 6, radius)
            }
        }
    }

    override fun localized() =
        this.javaClass.localized()
}