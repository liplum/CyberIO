package net.liplum.blocks.cyberion

import arc.graphics.Blending
import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Fill
import arc.graphics.g2d.Lines
import arc.math.Angles
import arc.math.Interp
import arc.math.Mathf
import arc.util.Time
import mindustry.gen.Building
import mindustry.graphics.Layer
import mindustry.world.draw.DrawBlock
import net.liplum.Var
import plumy.dsl.DrawLayer
import plumy.core.math.smooth

class DrawCyberionAgglomeration : DrawBlock() {
    var flameColor: Color = Var.HologramDark
    var midColor: Color = Var.Hologram
    var flameRad = 1f
    var flameRadiusScl = 5f
    var circleStroke = 1.5f
    var alpha = 0.68f
    var coreRadius = 5f
    var particles = 25
    var particleLife = 40f
    var particleRad = 10f
    var particleStroke = 1.1f
    var particleRadius = 2f
    var drawCenter = true
    var blending: Blending = Blending.additive
    override fun draw(build: Building) {
        DrawLayer(Layer.effect) {
            if (build.warmup() > 0f && flameColor.a > 0.001f) {
                Lines.stroke(circleStroke * build.warmup())
                val si = Mathf.absin(flameRadiusScl, 0.8f)
                val a = alpha * build.warmup()
                Draw.blend(blending)
                if (drawCenter) {
                    val progress = build.progress()
                    val smoothPro = progress.smooth
                    if (progress <= 0.8f) {
                        Draw.color(midColor, a)
                        Fill.circle(
                            build.x, build.y,
                            (flameRad + si + coreRadius) * smoothPro
                        )
                        Draw.color(flameColor, a)
                        Fill.circle(
                            build.x, build.y,
                            ((flameRad + si) * build.warmup())
                        )
                    } else { // > 0.8
                        val alphaScale = ((1f - progress) * 5f).smooth // [0,0.2] -> [0,1]
                        val smoothMax = 0.8f.smooth
                        Draw.color(midColor, a * alphaScale)
                        Fill.circle(
                            build.x, build.y,
                            (flameRad + si + coreRadius) * smoothMax
                        )
                        Draw.color(flameColor, a)
                        Fill.circle(
                            build.x, build.y,
                            ((flameRad + si) * build.warmup())
                        )
                    }
                }
                Draw.color(flameColor, a)
                Lines.stroke(particleStroke * build.warmup())
                val base = Time.time / particleLife
                rand.setSeed(build.id.toLong())
                for (i in 0 until particles) {
                    val fin = (rand.random(1f) + base) % 1f
                    val fout = 1f - fin
                    val angle = rand.random(360f)
                    val len = particleRad * Interp.pow2Out.apply(fout)
                    Fill.circle(
                        build.x + Angles.trnsx(angle, len),
                        build.y + Angles.trnsy(angle, len),
                        particleRadius * fin * build.warmup()
                    )
                }
                Draw.blend()
                Draw.reset()
            }
        }
    }
}
