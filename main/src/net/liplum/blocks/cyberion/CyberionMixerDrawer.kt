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
import mindustry.world.draw.DrawBlock
import net.liplum.S

class DrawCyberionMixer : DrawBlock() {
    var flameColor: Color = S.HologramDark
    var midColor: Color = S.Hologram
    var flameRad = 1f
    var circleSpace = 2f
    var flameRadiusScl = 5f
    var flameRadiusMag = 0.8f
    var circleStroke = 1.5f
    var alpha = 0.68f
    var particles = 25
    var particleLife = 40f
    var particleRad = 10f
    var particleStroke = 1.1f
    var particleLen = 5f
    var drawCenter = true
    var blending: Blending = Blending.additive
    override fun draw(build: Building) {
        if (build.warmup() > 0f && flameColor.a > 0.001f) {
            Lines.stroke(circleStroke * build.warmup())
            val si = Mathf.absin(flameRadiusScl, 0.8f)
            val a = alpha * build.warmup()
            Draw.blend(blending)
            Draw.color(midColor, a)
            if (drawCenter) Fill.circle(build.x, build.y, flameRad + si)
            Draw.color(flameColor, a)
            if (drawCenter) Lines.circle(build.x, build.y, (flameRad + circleSpace + si) * build.warmup())
            Lines.stroke(particleStroke * build.warmup())
            val base = Time.time / particleLife
            rand.setSeed(build.id.toLong())
            for (i in 0 until particles) {
                val fin = (rand.random(1f) + base) % 1f
                val fout = 1f - fin
                val angle = rand.random(360f)
                val len = particleRad * Interp.pow2Out.apply(fout)
                Lines.lineAngle(
                    build.x + Angles.trnsx(angle, len),
                    build.y + Angles.trnsy(angle, len),
                    angle,
                    particleLen * fin * build.warmup()
                )
            }
            Draw.blend()
            Draw.reset()
        }
    }
}
