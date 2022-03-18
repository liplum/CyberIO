package net.liplum.bullets

import arc.graphics.g2d.Draw
import arc.graphics.gl.Shader
import arc.math.Mathf
import arc.util.Tmp
import mindustry.entities.bullet.BasicBulletType
import mindustry.gen.Bullet
import net.liplum.shaders.on

open class ShaderBasicBulletT : BasicBulletType {
    @JvmField var shader: Shader? = null
    @JvmField var preShader: (Shader, Bullet) -> Unit = { _, _ -> }

    constructor(speed: Float, damage: Float, bulletSprite: String)
            : super(speed, damage) {
        sprite = bulletSprite
    }

    constructor(speed: Float, damage: Float) :
            this(speed, damage, "bullet")

    constructor() :
            this(1f, 1f, "bullet")

    override fun draw(b: Bullet) {
        drawTrail(b)
        val shader = shader
        shader.on {
            preShader(shader!!, b)
            val height = height * (1f - shrinkY + shrinkY * b.fout())
            val width = width * (1f - shrinkX + shrinkX * b.fout())
            val offset = -90 + if (spin != 0f) Mathf.randomSeed(b.id.toLong(), 360f) + b.time * spin else 0f
            val mix = Tmp.c1.set(mixColorFrom).lerp(mixColorTo, b.fin())

            Draw.mixcol(mix, mix.a)

            Draw.color(backColor)
            Draw.rect(backRegion, b.x, b.y, width, height, b.rotation() + offset)
            Draw.color(frontColor)
            Draw.rect(frontRegion, b.x, b.y, width, height, b.rotation() + offset)

            Draw.reset()
        }
    }
}