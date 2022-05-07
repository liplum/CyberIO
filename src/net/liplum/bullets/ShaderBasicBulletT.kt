package net.liplum.bullets

import arc.graphics.g2d.Draw
import arc.graphics.gl.Shader
import arc.math.Mathf
import arc.util.Tmp
import mindustry.entities.bullet.BasicBulletType
import mindustry.gen.Bullet
import net.liplum.lib.shaders.on

open class ShaderBasicBulletT<TS : Shader> : BasicBulletType {
    lateinit var shader: () -> TS
    @JvmField var preShader: (TS, Bullet) -> Unit = { _, _ -> }

    constructor(speed: Float, damage: Float, bulletSprite: String)
            : super(speed, damage, bulletSprite)

    constructor(speed: Float, damage: Float) :
            this(speed, damage, "bullet")

    constructor() : this(1f, 1f, "bullet")

    override fun draw(b: Bullet) {
        val shader = shader()
        shader.on {
            preShader(it, b)
            drawTrail(b)
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