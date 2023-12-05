package net.liplum.blocks.deleter

import arc.graphics.g2d.Draw
import arc.math.Mathf
import arc.util.Tmp
import mindustry.content.Fx
import mindustry.entities.Effect
import mindustry.entities.bullet.BasicBulletType
import mindustry.gen.*
import mindustry.graphics.Layer
import net.liplum.Var
import net.liplum.api.IExecutioner
import net.liplum.math.quadratic
import plumy.animation.ContextDraw.DrawScale
import net.liplum.utils.lostHp
import plumy.dsl.NewEffect

private val P2Alpha = quadratic(0.95f, 0.35f)
val deleted = NewEffect(60f) {
    Draw.alpha(fout())
    Icon.trash.region.DrawScale(x, y, scale = 1f + fin() * 1.5f)
}.apply {
    layer(Layer.bullet - 1f)
}

open class DeleterWave(
    override val executeProportion: Float,
    var extraLostHpBounce: Float,
) : BasicBulletType(), IExecutioner {
    var deletedFx: Effect = Fx.none

    init {
        hitEffect = Fx.hitLancer
        frontColor = Var.Hologram
        backColor = Var.HologramDark
        pierce = true
        pierceCap = 10
        lightRadius = 1f
        absorbable = false
        reflectable = false
        collidesAir = true
        collidesGround = true
        shrinkX = -5f
        shrinkY = 0f
        width = 10f
        height = 5f

        speed = 1.5f
        lifetime = 128f
        hitSize = 8f
        ammoMultiplier = 1f

        damage = 1f
    }

    override fun despawned(b: Bullet) {
    }

    open fun onHitTarget(b: Bullet, entity: Healthc) {
        if (entity.canBeExecuted) {
            execute(entity)
            deletedFx.at(b.x, b.y)
        } else {
            entity.damage(b.damage)
            entity.damagePierce(entity.lostHp * extraLostHpBounce)
        }
    }

    override fun hitTile(b: Bullet, build: Building, x: Float, y: Float, initialHealth: Float, direct: Boolean) {
        onHitTarget(b, build)
    }

    override fun hitEntity(b: Bullet, entity: Hitboxc, health: Float) {
        if (entity is Healthc) {
            onHitTarget(b, entity)
        }
    }

    override fun draw(b: Bullet) {
        drawTrail(b)
        val height = height * (1f - shrinkY + shrinkY * b.fout())
        val width = width * (1f - shrinkX + shrinkX * b.fout())
        val offset = -90 + if (spin != 0f) Mathf.randomSeed(b.id.toLong(), 360f) + b.time * spin else 0f
        val mix = Tmp.c1.set(mixColorFrom).lerp(mixColorTo, b.fin())

        Draw.mixcol(mix, mix.a)
        val a = (P2Alpha(b.fout()))
        Draw.color(backColor)
        Draw.alpha(a)
        Draw.rect(backRegion, b.x, b.y, width, height, b.rotation() + offset)
        Draw.color(frontColor)
        Draw.alpha(a)
        Draw.rect(frontRegion, b.x, b.y, width, height, b.rotation() + offset)

        Draw.reset()
    }
}