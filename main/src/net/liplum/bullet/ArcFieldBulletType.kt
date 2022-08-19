package net.liplum.bullet

import arc.graphics.g2d.Draw
import arc.graphics.g2d.Fill
import arc.math.Angles
import arc.math.Interp
import arc.util.Time
import arc.util.Tmp
import mindustry.entities.Damage
import mindustry.entities.Units
import mindustry.entities.bullet.ContinuousBulletType
import mindustry.gen.Building
import mindustry.gen.Bullet
import mindustry.gen.Healthc
import mindustry.gen.Hitboxc
import mindustry.world.blocks.ControlBlock
import plumy.core.arc.lighten
import plumy.core.math.Degree
import plumy.core.math.invoke

class ArcFieldBulletType : ContinuousBulletType() {
    @JvmField var angle: Degree = 80f
    @JvmField var lengthInterp: Interp = Interp.slope
    @JvmField var fieldAlpha = 0.3f
    @JvmField var highlightTime = damageInterval * 5f
    @JvmField var pointField = true
    @JvmField var pointFieldEnabledWhenPlayer = true
    @JvmField var lightenIntensity = -1f

    init {
        optimalLifeFract = 0.5f
        removeAfterPierce = false
        pierceArmor = true
    }

    override fun init() {
        super.init()
        if (!pointField)
            pointFieldEnabledWhenPlayer = false
    }

    override fun init(b: Bullet) {
        super.init(b)
        b.fdata = highlightTime
    }

    override fun update(b: Bullet) {
        super.update(b)
        b.fdata += Time.delta
    }

    override fun draw(b: Bullet) = b.run {
        val curLen = currentLength(this)
        if (lightenIntensity > 0f)
            Draw.color(Tmp.c1.set(hitColor).lighten(lightenIntensity))
        else Draw.color(hitColor)
        if (fdata < highlightTime) Draw.alpha(1f)
        else Draw.alpha(fieldAlpha * fin(lengthInterp))
        Fill.arc(x, y, curLen, angle / 360f, this.rotation() - angle / 2f)
        Draw.reset()
    }

    var hasCausedDamage = false
    override fun applyDamage(b: Bullet) = b.run {
        hasCausedDamage = false
        val curLen = currentLength(this)

        Units.nearbyEnemies(team, x, y, curLen) {
            tryHit(it)
        }
        if (collidesGround) {
            Units.nearbyBuildings(x, y, curLen) {
                if (it.team != this.team || collidesTeam)
                    tryHit(it)
            }
        }
        if (hasCausedDamage) {
            fdata = 0f
        }
    }

    fun Bullet.tryHit(t: Healthc) {
        val ang = rotation()
        val angToTarget = angleTo(t)
        if (Angles.within(ang, angToTarget, angle / 2f))
            Damage.collidePoint(this, team, hitEffect, t.x, t.y)
    }

    fun Bullet.hitTarget(target: Hitboxc) {
        target.collision(this, target.x, target.y)
        this.collision(target, target.x, target.y)
    }

    fun Bullet.hitTarget(target: Building) {
        target.collision(this)
        hit(this, target.x, target.y)
    }

    fun isControlledByPlayer(b: Bullet): Boolean {
        val turret = b.owner as? ControlBlock
        return turret?.isControlled ?: false
    }

    override fun currentLength(b: Bullet): Float = b.run {
        if (pointField) {
            if (pointFieldEnabledWhenPlayer && isControlledByPlayer(this)) {
                return@run Tmp.v1.set(aimX, aimY).sub(x, y).len() * lengthInterp(fin())
            } else {
                return@run length * lengthInterp(fin())
            }
        } else {
            return@run length * lengthInterp(fin())
        }
    }

    override fun drawLight(b: Bullet) {
    }

    companion object {
        inline operator fun invoke(config: ArcFieldBulletType.() -> Unit) =
            ArcFieldBulletType().apply(config)
    }
}