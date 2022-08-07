package net.liplum.bullet

import arc.graphics.g2d.Draw
import arc.graphics.g2d.Lines
import arc.math.Angles
import arc.math.Interp
import arc.util.Time
import mindustry.entities.Units
import mindustry.entities.bullet.ContinuousBulletType
import mindustry.gen.Bullet
import net.liplum.mdt.utils.MdtUnit
import plumy.core.math.Degree

class ArcFieldBulletType : ContinuousBulletType() {
    @JvmField var angle: Degree = 80f
    @JvmField var lengthInterp: Interp = Interp.slope
    @JvmField var fieldAlpha = 0.5f
    @JvmField var highlightTime = damageInterval * 5f

    init {
        optimalLifeFract = 0.5f
        removeAfterPierce = false
        pierceArmor = true
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
        Lines.stroke(curLen)
        Draw.color(hitColor)
        if (fdata < highlightTime) {
            Draw.alpha(1f) // breath
        } else {
            Draw.alpha(0.5f) // breath
        }
        Lines.arc(x, y, curLen / 2f, angle / 360f, this.rotation() - angle / 2f)
        Draw.reset()
    }

    var hasCausedDamage = false
    override fun applyDamage(b: Bullet) = b.run {
        hasCausedDamage = false
        val curLen = currentLength(this)
        Units.nearbyEnemies(team, x, y, curLen) {
            tryCauseDamageTo(it)
        }
        if (hasCausedDamage) {
            fdata = 0f
        }
    }

    fun Bullet.tryCauseDamageTo(unit: MdtUnit) {
        if (this.team != unit.team) {
            val aimAngle = rotation()
            val bullet2Enemy = this.angleTo(unit)
            if (Angles.within(aimAngle, bullet2Enemy, angle / 2f)) {
                damageTarget(unit)
                hasCausedDamage = true
            }
        }
    }

    fun Bullet.damageTarget(unit: MdtUnit) {
        unit.collision(this, unit.x, unit.y)
        this.collision(unit, unit.x, unit.y)
    }

    override fun currentLength(b: Bullet): Float {
        return length * b.fin(lengthInterp)
    }

    override fun drawLight(b: Bullet) {
    }
}