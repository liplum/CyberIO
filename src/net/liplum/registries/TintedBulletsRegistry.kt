package net.liplum.registries

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Fill
import arc.graphics.g2d.Lines
import arc.math.Angles
import arc.math.Mathf
import mindustry.content.Bullets
import mindustry.entities.Effect
import mindustry.entities.Effect.EffectContainer
import mindustry.entities.bullet.BulletType
import net.liplum.R
import net.liplum.blocks.prism.RgbList
import net.liplum.blocks.prism.SmallRgbFx
import net.liplum.blocks.prism.registerRGBIndex

fun shootSmallFlameGen(lifetime: Float, clipSize: Float, fg: Color, bk: Color): Effect =
    Effect(lifetime, clipSize) { e: EffectContainer ->
        Draw.color(fg, bk, Color.gray, e.fin())
        Angles.randLenVectors(
            e.id.toLong(), 8, e.finpow() * 60f, e.rotation, 10f
        ) { x: Float, y: Float -> Fill.circle(e.x + x, e.y + y, 0.65f + e.fout() * 1.5f) }
    }

val SmallRgbFx = RgbList {
    shootSmallFlameGen(32f, 80f, R.C.PrismRgbFG[it], R.C.PrismRgbBK[it])
}

fun hitFlameSmallGen(lifetime: Float, fg: Color, bk: Color): Effect =
    Effect(lifetime) { e: EffectContainer ->
        Draw.color(fg, bk, e.fin())
        Lines.stroke(0.5f + e.fout())
        Angles.randLenVectors(
            e.id.toLong(),
            2,
            1f + e.fin() * 15f,
            e.rotation,
            50f
        ) { x: Float, y: Float ->
            val ang = Mathf.angle(x, y)
            Lines.lineAngle(e.x + x, e.y + y, ang, e.fout() * 3 + 1f)
        }
    }

val hitFlameSmallFx = RgbList {
    hitFlameSmallGen(14f, R.C.PrismRgbFG[it], R.C.PrismRgbBK[it])
}

fun shootPyraFlameGen(lifetime: Float, clipSize: Float, fg: Color, bk: Color): Effect =
    Effect(lifetime, clipSize) { e: EffectContainer ->
        Draw.color(fg, bk, Color.gray, e.fin())
        Angles.randLenVectors(
            e.id.toLong(), 10, e.finpow() * 70f, e.rotation, 10f
        ) { x: Float, y: Float -> Fill.circle(e.x + x, e.y + y, 0.65f + e.fout() * 1.6f) }
    }

val shootPyraFlameFx = RgbList {
    shootPyraFlameGen(33f, 80f, R.C.PrismRgbFG[it], R.C.PrismRgbBK[it])
}

fun tintedBulletsRegistryLoad() {
    if (false) {
        Bullets.basicFlame.registerRGBIndex {
            (this.copy() as BulletType).apply {
                shootEffect = SmallRgbFx[it]
                hitEffect = hitFlameSmallFx[it]
            }
        }
        Bullets.pyraFlame.registerRGBIndex {
            (this.copy() as BulletType).apply {
                shootEffect = shootPyraFlameFx[it]
                hitEffect = hitFlameSmallFx[it]
            }
        }
    }
}
