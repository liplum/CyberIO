package net.liplum.bullet

import arc.graphics.Color
import arc.graphics.Texture.TextureFilter
import arc.graphics.g2d.Draw
import arc.math.Mathf
import arc.math.geom.Vec2
import mindustry.entities.Damage
import mindustry.entities.Effect
import mindustry.entities.Lightning
import mindustry.entities.bullet.BulletType
import mindustry.gen.Bullet
import plumy.animation.ContextDraw.DrawScale
import plumy.core.assets.EmptyTR
import plumy.core.assets.TR
import plumy.dsl.sprite

class BBulletType() : BulletType() {
    var scale: (Bullet) -> Float = { 1f }
    var textureName = ""
    var texture: TR = EmptyTR
    var color: Color = Color(1f, 1f, 1f, 0f)
    var enableRotate = true
    var filter: TextureFilter? = null

    constructor(trName: String) : this() {
        textureName = trName
    }

    override fun load() {
        texture = textureName.sprite
        val filter = filter
        if (filter != null) {
            texture.texture.setFilter(filter)
        }
    }

    init {
        hitColor = color
    }

    override fun draw(b: Bullet) {
        super.draw(b)
        Draw.z(layer)
        Draw.mixcol(color, color.a)
        texture.DrawScale(
            b.x, b.y, scale(b),
            if (enableRotate) b.rotation() else 0f
        )
    }

    override fun despawned(b: Bullet) {
        if (despawnHit) {
            hit(b)
        }

        if (!fragOnHit) {
            createFrags(b, b.x, b.y)
        }

        despawnEffect.at(b.x, b.y, b.rotation(), hitColor, scale(b))
        despawnSound.at(b)

        Effect.shake(despawnShake, despawnShake, b)
    }

    override fun hit(b: Bullet, x: Float, y: Float) {
        hitEffect.at(x, y, b.rotation(), hitColor, scale(b))
        hitSound.at(x, y, hitSoundPitch, hitSoundVolume)

        Effect.shake(hitShake, hitShake, b)

        if (fragOnHit) {
            createFrags(b, x, y)
        }
        createPuddles(b, x, y)
        createIncend(b, x, y)

        if (suppressionRange > 0) {
            //bullets are pooled, require separate Vec2 instance
            Damage.applySuppression(b.team, b.x, b.y, suppressionRange, suppressionDuration, 0f, suppressionEffectChance, Vec2(b.x, b.y))
        }

        createSplashDamage(b, x, y)

        for (i in 0 until lightning) {
            Lightning.create(
                b,
                lightningColor,
                if (lightningDamage < 0) damage else lightningDamage,
                b.x,
                b.y,
                b.rotation() + Mathf.range(lightningCone / 2) + lightningAngle,
                lightningLength + Mathf.random(lightningLengthRand)
            )
        }
    }
}