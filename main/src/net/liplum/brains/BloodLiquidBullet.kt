package net.liplum.brains

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Fill
import arc.math.Mathf
import arc.math.geom.Geometry
import arc.util.Tmp
import mindustry.Vars
import mindustry.content.Fx
import mindustry.entities.Fires
import mindustry.entities.Mover
import mindustry.entities.bullet.BulletType
import mindustry.entities.bullet.LiquidBulletType
import mindustry.entities.pattern.ShootPattern
import mindustry.gen.Bullet
import net.liplum.mdt.mixin.Mover

@Deprecated(
    "Just for design.",
    ReplaceWith("net.liplum.brains.Heartbeat")
)
open class HeartBeatShootPattern : ShootPattern() {
    /**
     * The force of systole
     */
    var systole = 0.15f
    var diastole = 3.5f
    var minIn = 0.02f
    var systoleTime = 1f
    var offset = 0f
    val systoleMover: Mover = Mover {
        if (timer.get(5, systoleTime)) {
            vel.scl(1f - systole)
        }
        if (vel.len() < minIn) {
            vel.setLength(diastole)
        }
    }

    override fun shoot(totalShots: Int, handler: BulletHandler) {
        val perAngle = 360f / shots
        val offset = Tmp.v1.set(offset, 0f)
        for (i in 0 until shots) {
            val angle = perAngle * i
            offset.setAngle(angle)
            handler.shoot(
                0f + offset.x,
                0f + offset.y,
                angle,
                firstShotDelay + shotDelay * i,
                systoleMover
            )
        }
    }
}
@Deprecated(
    "Blood bullet doesn't use liquid effect anymore",
    ReplaceWith("net.liplum.bullets.BBulletType")
)
/**
 * Copy from [LiquidBulletType]
 */
open class BloodLiquidBullet : BulletType() {
    var boilTime = 5f
    var blood: Blood = Blood.X
    var extinguishIntensity = 400f
    var orbSize = 3f
    override fun update(b: Bullet) {
        super.update(b)
        if (blood.willBoil() && b.time >= Mathf.randomSeed(b.id.toLong(), boilTime)) {
            Fx.vaporSmall.at(b.x, b.y, blood.gasColor)
            b.remove()
            return
        }
        if (blood.canExtinguish()) {
            val tile = Vars.world.tileWorld(b.x, b.y)
            if (tile != null && Fires.has(tile.x.toInt(), tile.y.toInt())) {
                Fires.extinguish(tile, 100f)
                b.remove()
                hit(b)
            }
        }
    }

    override fun draw(b: Bullet) {
        super.draw(b)
        if (blood.willBoil()) {
            Draw.color(blood.color, Tmp.c3.set(blood.gasColor).a(0.4f), b.time / Mathf.randomSeed(b.id.toLong(), boilTime))
            Fill.circle(b.x, b.y, orbSize * (b.fin() * 1.1f + 1f))
        } else {
            Draw.color(blood.color, Color.white, b.fout() / 100f)
            Fill.circle(b.x, b.y, orbSize)
        }
        Draw.reset()
    }

    override fun despawned(b: Bullet) {
        super.despawned(b)
        if (!blood.willBoil()) {
            hitEffect.at(b.x, b.y, b.rotation(), blood.color)
        }
    }

    override fun hit(b: Bullet, hitx: Float, hity: Float) {
        hitEffect.at(hitx, hity, blood.color)
        if (blood.temperature <= 0.5f && blood.flammability < 0.3f) {
            Fires.extinguish(Vars.world.tileWorld(hitx, hity), extinguishIntensity)
            for (p in Geometry.d4) {
                Fires.extinguish(Vars.world.tileWorld(hitx + p.x * Vars.tilesize, hity + p.y * Vars.tilesize), extinguishIntensity)
            }
        }
    }
}