package net.liplum.brains

import arc.graphics.g2d.Draw
import arc.graphics.g2d.Fill
import arc.graphics.g2d.Lines
import arc.math.Angles
import arc.math.Mathf
import mindustry.entities.Effect
import mindustry.graphics.Drawf
import net.liplum.R

object BrainFx {
    val eyeCharge = Effect(38f) {
        Draw.color(R.C.RedDark)
        Angles.randLenVectors(
            it.id.toLong(), 2, 1f + 20f * it.fout(), it.rotation, 120f
        ) { x: Float, y: Float ->
            Lines.lineAngle(
                it.x + x,
                it.y + y,
                Mathf.angle(x, y),
                it.fslope() * 3f + 1f
            )
        }
    }
    val eyeChargeBegin = Effect(60f) {
        Draw.color(R.C.RedDark)
        Fill.circle(it.x, it.y, it.fin() * 3f)
    }

    val eyeShoot = Effect(21f) {
        Draw.color(R.C.RedAlert)
        for (i in Mathf.signs) {
            Drawf.tri(it.x, it.y, 4f * it.fout(), 29f, it.rotation + 90f * i)
        }
    }
}