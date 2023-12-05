package net.liplum.holo

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Fill
import arc.graphics.g2d.Lines
import arc.math.geom.Intersector
import arc.scene.ui.layout.Table
import arc.util.Time
import mindustry.Vars
import mindustry.content.Fx
import mindustry.entities.abilities.Ability
import mindustry.gen.Bullet
import mindustry.gen.Groups
import mindustry.gen.Unit
import mindustry.graphics.Layer
import mindustry.graphics.Pal
import mindustry.ui.Bar
import net.liplum.Meta
import net.liplum.Var
import net.liplum.common.shader.use
import net.liplum.utils.healthPct
import net.liplum.utils.localized
import net.liplum.registry.SD

open class HoloForceField(
    val radius: Float, val regen: Float, val max: Float, val cooldown: Float,
    val color: Color = Var.Hologram,
) : Ability() {
    override fun update(unit: Unit) {
        if (unit.shield < max) {
            unit.shield += Time.delta * regen
        }
        val realRange = realRange(unit)
        if (unit.shield > 0) {
            Groups.bullet.intersect(
                unit.x - realRange,
                unit.y - realRange,
                realRange * 2f,
                realRange * 2f,
            ) {
                absorbBullet(it, unit)
            }
        }
    }

    override fun draw(unit: Unit) {
        if (unit.shield <= 0) return
        if (Vars.renderer.animateShields) {
            val healthPct = unit.healthPct
            SD.Hologram.use(Layer.shields) {
                it.opacityNoise *= 2f - healthPct
                it.flickering = it.DefaultFlickering + (1f - healthPct)
                Fill.poly(unit.x, unit.y, 6, realRange(unit))
            }
        } else {
            val forcePct = forcePct(unit)
            Draw.z(Layer.shields)
            Draw.color(color, Color.white, forcePct.coerceIn(0f, 1f) * 0.5f)
            Lines.stroke(1.5f)
            Draw.alpha(0.09f)
            Fill.poly(unit.x, unit.y, 6, radius * forcePct)
            Draw.alpha(1f)
            Lines.poly(unit.x, unit.y, 6, radius * forcePct)
        }
    }

    override fun displayBars(unit: Unit, bars: Table) {
        bars.add(Bar("stat.shieldhealth", Pal.accent) { unit.shield / max }).row()
    }

    open fun forcePct(unit: Unit): Float =
        unit.shield / max

    open fun realRange(unit: Unit): Float =
        radius * forcePct(unit)

    override fun localized() =
        this.javaClass.localized(Meta.ModID)

    fun absorbBullet(bullet: Bullet, unit: Unit): Boolean {
        val realRange = realRange(unit)
        if (
            bullet.type.absorbable &&
            bullet.team != unit.team &&
            unit.shield > 0 &&
            Intersector.isInsideHexagon(unit.x, unit.y, realRange * 2f, bullet.x, bullet.y)
        ) {
            bullet.absorb()
            Fx.absorb.at(bullet)
            //break shield
            if (unit.shield <= bullet.damage()) {
                unit.shield -= cooldown * regen
                HoloFx.shieldBreak.at(unit.x, unit.y, realRange, color, unit)
            }
            unit.shield -= bullet.damage()
            return true
        }
        return false
    }
}