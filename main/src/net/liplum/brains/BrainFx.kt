package net.liplum.brains

import arc.graphics.Texture.TextureFilter
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Fill
import arc.graphics.g2d.Lines
import arc.math.Angles
import arc.math.Mathf
import mindustry.entities.Effect
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import net.liplum.Cio
import net.liplum.R
import net.liplum.ResourceLoader
import net.liplum.lib.DrawSize
import net.liplum.lib.TR
import net.liplum.lib.utils.progress
import net.liplum.mdt.utils.sheet

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
    var bloodBulletFrames: Array<TR> = emptyArray()
    val bloodBulletHit = Effect(30f) {
        val scale = it.data as? Float ?: -1f
        Draw.mixcol(it.color, it.color.a)
        bloodBulletFrames.progress(it.fin())
            .DrawSize(it.x, it.y, if (scale < 0f) 1f else scale)
    }.apply {
        layer = Layer.bullet - 0.1f
    }
    @JvmStatic
    fun load() {
        ResourceLoader += {
            bloodBulletFrames = "blood-bullet-hit".Cio.sheet(16)
            if (bloodBulletFrames.isNotEmpty())
                bloodBulletFrames[0].texture.setFilter(TextureFilter.nearest)
        }
    }
}