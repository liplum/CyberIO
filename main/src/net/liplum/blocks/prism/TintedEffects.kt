package net.liplum.blocks.prism

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Fill
import arc.graphics.g2d.Lines
import arc.math.Angles
import arc.math.Mathf
import mindustry.content.UnitTypes.block
import mindustry.entities.Effect
import mindustry.entities.Effect.EffectContainer
import mindustry.world.Block

val SmallRgbFx = RgbList {
    shootSmallRGBGen(3.35f, 17f, FG(it), BK(it))
}
val ShootRgbFx = RgbList {
    shootRGBGen(4f, 60f, FG(it), BK(it))
}
val HitBulletBigRgbFx = RgbList {
    hitBulletBigRGBGen(13f, FG(it), BK(it))
}
val HitMeltRgbFx = RgbList {
    hitMeltRGBGen(13f, BK(it))
}
val healBlockFullFx = RgbList {
    healBlockFullGen(20f, BK(it))
}

fun shootSmallRGBGen(lifetime: Float, clipSize: Float, fg: Color, bk: Color): Effect =
    Effect(lifetime, clipSize) {
        Draw.color(fg, bk, Color.gray, it.fin())

        Angles.randLenVectors(
            it.id.toLong(), 8, it.finpow() * 60f, it.rotation, 10f
        ) { x: Float, y: Float ->
            Fill.circle(it.x + x, it.y + y, 0.65f + it.fout() * 1.5f)
        }
    }

fun shootRGBGen(lifetime: Float, clipSize: Float, fg: Color, bk: Color): Effect =
    Effect(lifetime, clipSize) {
        Draw.color(fg, bk, Color.gray, it.fin())

        Angles.randLenVectors(
            it.id.toLong(), 10, it.finpow() * 70f, it.rotation, 10f
        ) { x: Float, y: Float ->
            Fill.circle(it.x + x, it.y + y, 0.65f + it.fout() * 1.6f)
        }
    }

fun hitBulletBigRGBGen(lifetime: Float, fg: Color, bk: Color): Effect =
    Effect(lifetime) { e: EffectContainer ->
        Draw.color(fg, bk, e.fin())
        Lines.stroke(0.5f + e.fout() * 1.5f)
        Angles.randLenVectors(
            e.id.toLong(),
            8,
            e.finpow() * 30f,
            e.rotation,
            50f
        ) { x: Float, y: Float ->
            val ang = Mathf.angle(x, y)
            Lines.lineAngle(e.x + x, e.y + y, ang, e.fout() * 4 + 1.5f)
        }
    }

fun hitMeltRGBGen(lifetime: Float, color: Color): Effect =
    Effect(lifetime) { e: EffectContainer ->
        Draw.color(color)
        Lines.stroke(e.fout() * 2f)
        Angles.randLenVectors(e.id.toLong(), 6, e.finpow() * 18f) { x: Float, y: Float ->
            val ang = Mathf.angle(x, y)
            Lines.lineAngle(e.x + x, e.y + y, ang, e.fout() * 4 + 1f)
        }
    }

fun healBlockFullGen(lifetime: Float, color: Color): Effect =
    Effect(lifetime) { e: EffectContainer ->
        if (e.data !is Block) return@Effect
        Draw.mixcol(color, 1f)
        Draw.alpha(e.fout())
        Draw.rect(block.fullIcon, e.x, e.y)
    }