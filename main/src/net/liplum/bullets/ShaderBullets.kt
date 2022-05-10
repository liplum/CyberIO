package net.liplum.bullets

import arc.graphics.gl.Shader
import mindustry.entities.bullet.BasicBulletType
import mindustry.entities.bullet.ContinuousLaserBulletType
import mindustry.gen.Bullet
import net.liplum.lib.shaders.on

open class ShaderCLaserT<TS : Shader>(damage: Float) : ContinuousLaserBulletType(damage) {
    constructor() : this(0f)

    @JvmField var preShader: (TS, Bullet) -> Unit = { _, _ -> }
    lateinit var shader: () -> TS
    override fun draw(b: Bullet) {
        shader().on {
            preShader(it, b)
            super.draw(b)
        }
    }
}

open class ShaderBasicBulletT<TS : Shader> : BasicBulletType {
    lateinit var shader: () -> TS
    @JvmField var preShader: (TS, Bullet) -> Unit = { _, _ -> }

    constructor(speed: Float, damage: Float, bulletSprite: String)
            : super(speed, damage, bulletSprite)

    constructor(speed: Float, damage: Float) :
            this(speed, damage, "bullet")

    constructor() : this(1f, 1f, "bullet")

    override fun draw(b: Bullet) {
        shader().on {
            preShader(it, b)
            super.draw(b)
        }
    }
}