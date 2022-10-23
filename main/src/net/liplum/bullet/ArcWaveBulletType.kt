package net.liplum.bullet

import arc.graphics.g2d.Draw
import arc.graphics.g2d.Lines
import arc.math.Angles
import arc.math.Mathf
import arc.math.geom.Vec2
import arc.util.Tmp
import mindustry.content.Fx
import mindustry.entities.Units
import mindustry.entities.bullet.BulletType
import mindustry.gen.Bullet
import mindustry.graphics.Layer
import net.liplum.DebugOnly
import plumy.dsl.DrawLayer
import net.liplum.render.G
import plumy.core.MUnit
import plumy.core.math.Degree
import plumy.core.math.Radius
import plumy.core.math.plusAssign
import plumy.core.math.smooth
import java.util.Vector

class ArcWaveBulletType : BulletType {
    constructor(speed: Float, damage: Float) : super(speed, damage)
    constructor() : super()

    @JvmField var damageInterval = 5f
    @JvmField var angle: Degree = 80f
    @JvmField var thickness = 6f
    @JvmField var radius: Radius = 60f
    @JvmField var expandSpeed = 3.5f

    init {
        removeAfterPierce = false
        pierceCap = -1
        despawnEffect = Fx.none
        shootEffect = Fx.none
        impact = true
        keepVelocity = false
        collides = false
        pierce = true
        hittable = false
        absorbable = false
        pierceArmor = true
    }

    override fun continuousDamage(): Float =
        damage / damageInterval * 60f

    override fun estimateDPS(): Float =
        damage * 100f / damageInterval * 3f

    override fun init() {
        super.init()
        drawSize = drawSize.coerceAtLeast(angle)
    }

    val tmp = Vec2()
    fun findArcCenter(b: Bullet): Vec2 = b.run {
        val o = tmp.set(this)
        val delta = Tmp.v1.set(radius, 0f).setAngle(rotation()).inv()
        o += delta
        o
    }

    override fun draw(b: Bullet) = b.run {
        super.draw(b)
        val rotation = rotation()
        val o = findArcCenter(this)
        DebugOnly {
            DrawLayer(Layer.overlayUI) {
                G.circle(o.x, o.y, 2f)
            }
        }
        val attenuation = ((1f - time / lifetime) + 0.2f).smooth
        Lines.stroke(thickness * attenuation)
        Draw.color(trailColor)
        val curAngle = (time * expandSpeed / lifetime).smooth.coerceAtLeast(0.2f) * angle
        Lines.arc(o.x, o.y, radius, curAngle / 360f, rotation - curAngle / 2f)
        Draw.reset()
    }

    override fun update(b: Bullet) {
        //damage every 5 ticks
        /*if (b.timer(1, damageInterval)) {
            applyDamage(b)
        }*/
    }

    fun applyDamage(b: Bullet) = b.run {
        Units.nearbyEnemies(team, x, y, (angle * Mathf.PI) / 180f) {
            tryCauseDamageTo(it)
        }
    }

    fun Bullet.tryCauseDamageTo(unit: MUnit) {
        if (this.team != unit.team) {
            val aimAngle = rotation()
            val bullet2Enemy = this.angleTo(unit)
            if (Angles.within(aimAngle, bullet2Enemy, angle / 2f)) {
                damageTarget(unit)
            }
        }
    }

    fun Bullet.damageTarget(unit: MUnit) {
        unit.collision(this, unit.x, unit.y)
        this.collision(unit, unit.x, unit.y)
    }

    companion object {
        inline operator fun invoke(
            speed: Float = 1f,
            damage: Float = 1f,
            config: ArcWaveBulletType.() -> Unit,
        ) = ArcWaveBulletType(speed, damage).apply(config)
    }
}