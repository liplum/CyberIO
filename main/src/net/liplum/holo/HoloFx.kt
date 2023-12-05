package net.liplum.holo

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Fill
import arc.graphics.g2d.Lines
import mindustry.Vars
import mindustry.entities.Effect
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import net.liplum.Var
import net.liplum.common.shader.on
import net.liplum.registry.SD

object HoloFx {
    val heal = Effect(11f) {
        Draw.color(Var.Hologram)
        Lines.stroke(it.fout() * 2f)
        Lines.circle(it.x, it.y, 2f + it.finpow() * 7f)
    }
    val healWaveDynamic = Effect(22f) {
        Draw.color(Var.Hologram)
        Lines.stroke(it.fout() * 2f)
        Lines.circle(it.x, it.y, 4f + it.finpow() * it.rotation)
    }
    val hitLaser = Effect(8f) {
        Draw.color(Color.white, Var.Hologram, it.fin())
        Lines.stroke(0.5f + it.fout())
        Lines.circle(it.x, it.y, it.fin() * 5f)
        Drawf.light(it.x, it.y, 23f, Var.Hologram, it.fout() * 0.7f)
    }
    val shootHeal = Effect(8f) {
        Draw.color(Var.Hologram)
        val w = 1f + 5 * it.fout()
        Drawf.tri(it.x, it.y, w, 17f * it.fout(), it.rotation)
        Drawf.tri(it.x, it.y, w, 4f * it.fout(), it.rotation + 180f)
    }
    val shieldBreak: Effect = Effect(60f) {
        if (Vars.renderer.animateShields) {
            SD.Hologram.on(Layer.shields) {
                Fill.poly(it.x, it.y, 6, it.rotation * it.fout())
                Draw.reset()
            }
        } else {
            Draw.z(Layer.shields)
            Draw.color(it.color)
            Lines.stroke(3f * it.fout())
            Lines.poly(it.x, it.y, 6, it.rotation + it.fin())
            Draw.reset()
        }
    }.followParent(true)
}
