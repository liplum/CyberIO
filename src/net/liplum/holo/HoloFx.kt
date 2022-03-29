package net.liplum.holo

import arc.Core
import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Fill
import arc.graphics.g2d.Lines
import mindustry.entities.Effect
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import net.liplum.R
import net.liplum.lib.shaders.SD
import net.liplum.lib.shaders.on

object HoloFx {
    val heal = Effect(11f) {
        Draw.color(R.C.Holo)
        Lines.stroke(it.fout() * 2f)
        Lines.circle(it.x, it.y, 2f + it.finpow() * 7f)
    }
    val healWaveDynamic = Effect(22f) {
        Draw.color(R.C.Holo)
        Lines.stroke(it.fout() * 2f)
        Lines.circle(it.x, it.y, 4f + it.finpow() * it.rotation)
    }
    val hitLaser = Effect(8f) {
        Draw.color(Color.white, R.C.Holo, it.fin())
        Lines.stroke(0.5f + it.fout())
        Lines.circle(it.x, it.y, it.fin() * 5f)
        Drawf.light(it.x, it.y, 23f, R.C.Holo, it.fout() * 0.7f)
    }
    val shootHeal = Effect(8f) {
        Draw.color(R.C.Holo)
        val w = 1f + 5 * it.fout()
        Drawf.tri(it.x, it.y, w, 17f * it.fout(), it.rotation)
        Drawf.tri(it.x, it.y, w, 4f * it.fout(), it.rotation + 180f)
    }
    val shieldBreak: Effect = Effect(60f) {
        if (Core.settings.getBool(R.Setting.AnimatedShields)) {
            SD.Hologram2.on(Layer.shields) {
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
