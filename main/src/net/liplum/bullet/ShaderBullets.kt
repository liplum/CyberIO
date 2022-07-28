package net.liplum.bullet

import arc.graphics.g2d.Draw
import arc.graphics.g2d.Lines
import arc.math.Mathf
import arc.util.Time
import arc.util.Tmp
import mindustry.entities.Damage
import mindustry.entities.bullet.BasicBulletType
import mindustry.entities.bullet.ContinuousLaserBulletType
import mindustry.gen.Bullet
import mindustry.graphics.Drawf
import net.liplum.common.shader.ShaderBase
import net.liplum.common.shader.use

open class ShaderCLaserT<TS : ShaderBase>(damage: Float) : ContinuousLaserBulletType(damage) {
    constructor() : this(0f)

    @JvmField var preShader: (TS, Bullet) -> Unit = { _, _ -> }
    lateinit var shader: () -> TS
    override fun draw(b: Bullet) {
        shader().use(layer) {
            preShader(it, b)
            val realLength = Damage.findLaserLength(b, length)
            val fout = Mathf.clamp(
                if (b.time > b.lifetime - fadeTime)
                    1f - (b.time - (lifetime - fadeTime)) / fadeTime
                else
                    1f
            )
            val baseLen = realLength * fout
            val rot = b.rotation()

            for (i in colors.indices) {
                Draw.color(Tmp.c1.set(colors[i]).mul(1f + Mathf.absin(Time.time, 1f, 0.1f)))
                val colorFin = i / (colors.size - 1).toFloat()
                val baseStroke = Mathf.lerp(strokeFrom, strokeTo, colorFin)
                val stroke = (width + Mathf.absin(Time.time, oscScl, oscMag)) * fout * baseStroke
                val ellipseLenScl = Mathf.lerp(1 - i / colors.size.toFloat(), 1f, pointyScaling)
                Lines.stroke(stroke)
                Lines.lineAngle(b.x, b.y, rot, baseLen - frontLength, false)
                //back ellipse
                Drawf.flameFront(b.x, b.y, divisions, rot + 180f, backLength, stroke / 2f)
                //front ellipse
                Tmp.v1.trnsExact(rot, baseLen - frontLength)
                Drawf.flameFront(b.x + Tmp.v1.x, b.y + Tmp.v1.y, divisions, rot, frontLength * ellipseLenScl, stroke / 2f)
            }

            Tmp.v1.trns(b.rotation(), baseLen * 1.1f)

            Drawf.light(b.x, b.y, b.x + Tmp.v1.x, b.y + Tmp.v1.y, lightStroke, lightColor, 0.7f)
            Draw.reset()
        }
    }
}

open class ShaderBasicBulletT<TS : ShaderBase> : BasicBulletType {
    lateinit var shader: () -> TS
    @JvmField var preShader: (TS, Bullet) -> Unit = { _, _ -> }

    constructor(speed: Float, damage: Float, bulletSprite: String)
            : super(speed, damage, bulletSprite)

    constructor(speed: Float, damage: Float) :
            this(speed, damage, "bullet")

    constructor() : this(1f, 1f, "bullet")

    override fun draw(b: Bullet) {
        shader().use(layer) {
            preShader(it, b)
            super.draw(b)
        }
    }
}