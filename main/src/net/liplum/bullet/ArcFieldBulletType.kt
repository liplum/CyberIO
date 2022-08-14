package net.liplum.bullet

import arc.graphics.g2d.Draw
import arc.graphics.g2d.Lines
import arc.math.Angles
import arc.math.Interp
import arc.util.Time
import arc.util.Tmp
import mindustry.entities.Units
import mindustry.entities.bullet.ContinuousBulletType
import mindustry.gen.Bullet
import mindustry.world.blocks.ControlBlock
import net.liplum.mdt.render.Text
import net.liplum.mdt.utils.MdtUnit
import plumy.core.arc.lighten
import plumy.core.math.Degree

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
        Lines.stroke(curLen)
        if (lightenIntensity > 0f)
            Draw.color(Tmp.c1.set(hitColor).lighten(lightenIntensity))
        else Draw.color(hitColor)
        if (fdata < highlightTime) Draw.alpha(1f)
        else Draw.alpha(fieldAlpha * b.fin(lengthInterp))
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

    fun isControlledByPlayer(b: Bullet): Boolean {
        val turret = b.owner as? ControlBlock
        return turret?.isControlled ?: false
    }

    override fun currentLength(b: Bullet): Float = b.run {
        if (pointField) {
            if (pointFieldEnabledWhenPlayer && isControlledByPlayer(this)) {
                return@run Tmp.v1.set(aimX, aimY).sub(x, y).len()
            } else {
                return@run length * b.fin(lengthInterp)
            }
        } else {
            return@run length * b.fin(lengthInterp)
        }
    }

    override fun drawLight(b: Bullet) {
    }

    companion object {
        inline operator fun invoke(config: ArcFieldBulletType.() -> Unit) =
            ArcFieldBulletType().apply(config)
    }
}