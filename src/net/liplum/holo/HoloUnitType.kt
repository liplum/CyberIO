package net.liplum.holo

import arc.graphics.g2d.Draw
import arc.math.Angles
import arc.math.Mathf
import mindustry.Vars
import mindustry.gen.Payloadc
import mindustry.gen.Unit
import mindustry.graphics.Layer
import mindustry.type.UnitType
import net.liplum.R
import net.liplum.shaders.SD
import net.liplum.shaders.use
import net.liplum.utils.healthPct
import kotlin.math.min

open class HoloUnitType(name: String) : UnitType(name) {
    @JvmField var GhostingBlend = true
    @JvmField var minAlpha = 0.15f

    init {
        //outlineColor = R.C.HoloDark
        lightColor = R.C.Holo
        engineColor = R.C.Holo
        healColor = R.C.Holo
        engineColorInner = R.C.HoloDark
        mechLegColor = R.C.HoloDark
    }

    override fun update(unit: Unit) {
        super.update(unit)
    }

    override fun draw(unit: Unit) {
        val z =
            if (unit.elevation > 0.5f) if (lowAltitude) Layer.flyingUnitLow else Layer.flyingUnit else groundLayer + Mathf.clamp(
                hitSize / 4000f,
                0f,
                0.01f
            )

        if (unit.controller().isBeingControlled(Vars.player.unit())) {
            drawControl(unit)
        }

        if (unit.isFlying || visualElevation > 0) {
            Draw.z(min(Layer.darkness, z - 1f))
            drawShadow(unit)
        }

        Draw.z(z - 0.02f)

        Draw.z(min(z - 0.01f, Layer.bullet - 1f))

        if (unit is Payloadc) {
            drawPayload(unit)
        }
        val healthPct = unit.healthPct
        val alpha = healthPct.coerceAtLeast(minAlpha)
        drawSoftShadow(unit, alpha)

        Draw.z(z)
        SD.hologram2.use {
            it.alpha = alpha
            it.opacityNoise *= 2f - healthPct
            it.flickering = it.DefaultFlickering + (1f - healthPct)

            it.blendFormerColor = GhostingBlend
            if (drawBody) {
                drawOutline(unit)
            }
            drawWeaponOutlines(unit)
            if (engineSize > 0) {
                drawEngine(unit)
            }
            if (drawBody) {
                drawBody(unit)
            }
            if (drawCell) {
                drawCell(unit)
            }
            drawWeapons(unit)
        }
        if (drawItems) {
            drawItems(unit)
        }
        drawLight(unit)

        if (unit.shieldAlpha > 0 && drawShields) {
            drawShield(unit)
        }

        if (decals.size > 0) {
            val base = unit.rotation - 90
            for (d in decals) {
                Draw.z(d.layer)
                Draw.scl(d.xScale, d.yScale)
                Draw.color(d.color)
                Draw.rect(
                    d.region,
                    unit.x + Angles.trnsx(base, d.x, d.y),
                    unit.y + Angles.trnsy(base, d.x, d.y),
                    base + d.rotation
                )
            }
            Draw.reset()
            Draw.z(z)
        }

        if (unit.abilities.size > 0) {
            for (a in unit.abilities) {
                Draw.reset()
                a.draw(unit)
            }
        }

        Draw.reset()
    }
}