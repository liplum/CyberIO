package net.liplum.bullet

import arc.func.Cons
import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Fill
import arc.graphics.g2d.Lines
import arc.math.Mathf
import arc.util.Time
import arc.util.Tmp
import mindustry.Vars
import mindustry.content.Fx
import mindustry.entities.Units
import mindustry.entities.bullet.BulletType
import mindustry.gen.Bullet
import mindustry.gen.Unit
import mindustry.graphics.Layer
import net.liplum.DebugOnly
import net.liplum.R
import net.liplum.api.bullets.BulletAbility
import plumy.dsl.DrawLayer
import net.liplum.render.G
import plumy.core.MUnit
import plumy.dsl.inTheWorld
import plumy.dsl.worldXY
import net.liplum.render.CioFx
import net.liplum.render.Shape

open class ProvidenceBA : BulletAbility() {
    var range = 25f
    var damage = 1f
    var lightAlpha = 0.4f
    var lightColor: Color = R.C.Providence
    protected var actionOnUnit: Cons<MUnit> = Cons {
        it.damageContinuousPierce(damage)
    }

    override fun update(b: Bullet) = b.run {
        Units.nearbyEnemies(team, x, y, range, actionOnUnit)
    }

    override fun draw(b: Bullet) = b.run {
        DrawLayer(Layer.bullet - 0.1f) {
            Draw.color(Tmp.c1.set(lightColor).lerp(bulletType.lightColor, 0.5f))
            Draw.alpha(lightAlpha)
            val range = range * (1f + G.sin / 9f) * 2f
            Draw.rect(Shape.motionCircle, x, y, range, range)
        }
        DebugOnly {
            G.circle(x, y, this@ProvidenceBA.range, alpha = 0.3f)
        }
        return@run
    }

    override fun preInit(type: BulletType) = type.run {
        speed *= 0.5f
        pierce = true
        pierceCap += 1
        drawSize = drawSize.coerceAtLeast(range)
    }
}

open class SlowDownBA : BulletAbility() {
    var range = 60f
    var slowDown = 0.2f
    var darkAlpha = 0.7f
    var darkColor: Color = R.C.Black
    protected var actionOnUnit: Cons<MUnit> = Cons {
        val original = it.vel.len()
        val new = original * slowDown
        val delta = (original - new) * Time.delta
        it.vel.setLength(original - delta)
    }

    override fun update(b: Bullet) = b.run {
        Units.nearbyEnemies(team, x, y, range, actionOnUnit)
    }

    override fun draw(b: Bullet) = b.run {
        DrawLayer(Layer.bullet - 0.2f) {
            val range = range * (1f + G.sin / 9f) * 2f
            Draw.color(darkColor)
            Draw.alpha(darkAlpha)
            Draw.rect(Shape.motionCircle, x, y, range, range)
        }
        DebugOnly {
            G.circle(x, y, range, alpha = 0.3f)
        }
        return@run
    }

    override fun preInit(type: BulletType) = type.run {
        pierce = true
        pierceCap += 1
        drawSize = drawSize.coerceAtLeast(range)
    }
}

open class InfiniteBA : BulletAbility() {
    override fun update(b: Bullet) = b.run {
        if (!b.inTheWorld()) {
            b.remove()
        }
        time = 0f
    }

    override fun preInit(type: BulletType) = type.run {
        pierce = true
        pierceCap += 1
    }
}

open class TeleportBA : BulletAbility() {
    var length = 20f
    var interval = 20f
    override fun update(b: Bullet) = b.run {
        if (b.timer.get(4, interval)) {
            Tmp.v1.set(b.vel).setLength(length).let {
                b.x += it.x
                b.y += it.y
            }
            Fx.smeltsmoke.at(this)
        }
    }

    override fun preInit(type: BulletType) = type.run {
        damage *= 1.2f
    }
}

open class SpinBA : BulletAbility() {
    var angleForce = 1f
    var isClockwise = true
    override fun update(b: Bullet) = b.run {
        val angleForce = if (isClockwise) -angleForce else angleForce
        vel.setAngle(vel.angle() + angleForce * Time.delta)
        return@run
    }

    override fun preInit(type: BulletType) = type.run {
        damage *= 0.8f
        speed *= 0.8f
    }
}

open class TileMovingBA : BulletAbility() {
    var moveInterval = 10f
    override fun update(b: Bullet) = b.run {
        if (timer.get(6, moveInterval)) {
            Tmp.v1.set(vel).nor()
            x += Tmp.v1.x * Vars.tilesize
            y += Tmp.v1.y * Vars.tilesize
        }
        tileOn()?.let {
            x = it.x.worldXY
            y = it.y.worldXY
        }
        return@run
    }
}

open class BlackHoleBA : BulletAbility() {
    var range = 50f
    override fun update(b: Bullet) = b.run {
        var absorbed = false
        Units.nearbyEnemies(team, x, y, range) {
            val angle = it.angleTo(this)
            it.vel.setLength(it.vel.len().coerceAtLeast(1f))
            it.vel.setAngle(angle)
            absorbed = true
        }
        if (absorbed && Mathf.chanceDelta((0.12f * Time.delta).toDouble()))
            CioFx.blackHoleAbsorbing.at(x, y, range * 10f, b)
    }

    override fun draw(b: Bullet) = b.run {
        DrawLayer(Layer.bullet - 0.1f) {
            Draw.color(R.C.Black)
            Draw.alpha(0.5f)
            val range = range * (1f + G.sin / 9f) * 2f
            Draw.rect(Shape.motionCircle, x, y, range, range)
        }
        DebugOnly {
            G.circle(x, y, range, alpha = 0.3f)
        }
        return@run
    }

    override fun preInit(type: BulletType) = type.run {
        drawSize = drawSize.coerceAtLeast(range)
        ammoMultiplier = 4f
        speed *= 0.5f
    }
}

open class RestrictedAreaBA : BulletAbility() {
    var range = 10f
    var shieldSide = 5
    var self: Bullet? = null
    protected var unitRestrictor = Cons { unit: Unit ->
        val overlapDst = unit.hitSize / 2f + range - unit.dst(self)
        if (overlapDst > 0) {
            //stop
            unit.vel.setZero()
            //get out
            unit.move(Tmp.v1.set(unit).sub(self).setLength(overlapDst + 0.01f))
            if (Mathf.chanceDelta((0.12f * Time.delta).toDouble())) {
                Fx.circleColorSpark.at(unit.x, unit.y, self!!.team.color)
            }
        }
    }

    override fun update(b: Bullet) = b.run {
        self = b
        Units.nearbyEnemies(team, x, y, range, unitRestrictor)
    }

    override fun draw(b: Bullet) = b.run {
        DrawLayer(Layer.shields) {
            if (Vars.renderer.animateShields) {
                Fill.poly(x, y, shieldSide, range)
            } else {
                Lines.stroke(1.5f)
                Draw.alpha(0.09f)
                Fill.poly(x, y, shieldSide, range)
                Draw.alpha(1f)
                Lines.poly(x, y, shieldSide, range)
                Draw.reset()
            }
        }
    }

    override fun preInit(type: BulletType) = type.run {
        drawSize = drawSize.coerceAtLeast(range)
        ammoMultiplier = 4f
    }
}
