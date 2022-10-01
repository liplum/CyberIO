package net.liplum.blocks.jammer

import arc.graphics.g2d.Draw
import arc.graphics.g2d.Lines
import arc.math.Mathf
import arc.util.Time
import arc.util.Tmp
import mindustry.entities.Damage
import mindustry.entities.bullet.ContinuousLaserBulletType
import mindustry.gen.Bullet
import mindustry.graphics.Drawf
import net.liplum.common.shader.use
import net.liplum.registry.SD

class JammingLaser(damage: Float) : ContinuousLaserBulletType(damage) {
    constructor() : this(0f)

    init {
        laserAbsorb = false
    }

    var spaceMag = 35f
    var tscales = floatArrayOf(1f, 0.7f, 0.5f, 0.2f)
    var strokes = floatArrayOf(2f, 1.5f, 1f, 0.3f)
    var lenscales = floatArrayOf(1f, 1.12f, 1.15f, 1.17f)
    override fun draw(b: Bullet) {
        SD.TvStatic.use(layer) {
            val realLength = Damage.findLaserLength(b, length)
            val fout =
                Mathf.clamp(
                    if (b.time > b.lifetime - fadeTime)
                        1f - (b.time - (lifetime - fadeTime)) / fadeTime
                    else
                        1f
                )
            val baseLen = realLength * fout
            Lines.lineAngle(b.x, b.y, b.rotation(), baseLen)
            for (s in colors.indices) {
                Draw.color(Tmp.c1.set(colors[s]).mul(1f + Mathf.absin(Time.time, 1f, 0.1f)))
                for (i in tscales.indices) {
                    Tmp.v1.trns(b.rotation() + 180f, (lenscales[i] - 1f) * spaceMag)
                    Lines.stroke((width + Mathf.absin(Time.time, oscScl, oscMag)) * fout * strokes[s] * tscales[i])
                    Lines.lineAngle(b.x + Tmp.v1.x, b.y + Tmp.v1.y, b.rotation(), baseLen * lenscales[i], false)
                }
            }
            Tmp.v1.trns(b.rotation(), baseLen * 1.1f)
            Drawf.light(b.x, b.y, b.x + Tmp.v1.x, b.y + Tmp.v1.y, lightStroke, lightColor, 0.7f)
            Draw.reset()
        }
    }

    companion object {
        inline operator fun invoke(config: JammingLaser.() -> Unit) =
            JammingLaser().apply(config)
    }
}