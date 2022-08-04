package net.liplum.bullet

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Lines
import arc.math.Interp
import mindustry.content.Fx
import mindustry.entities.Damage
import mindustry.entities.bullet.BulletType
import mindustry.gen.Bullet
import plumy.core.arc.Color
import net.liplum.lib.math.Degree
import net.liplum.mdt.render.G
import net.liplum.mdt.utils.WorldXY

class FieldBulletType : BulletType {
    constructor(speed: Float, damage: Float) : super(speed, damage)
    constructor() : super()

    @JvmField var continuous = true
    @JvmField var length: WorldXY = 160f
    @JvmField var angle: Degree = 80f
    @JvmField var lengthInterp: Interp = Interp.slope
    @JvmField var fieldColor = Color("#e189f5")
    @JvmField var fieldAlpha = 0.5f

    init {
        optimalLifeFract = 0.5f
        removeAfterPierce = false
        pierceCap = -1
        speed = 0f
        despawnEffect = Fx.none
        shootEffect = Fx.none
        lifetime = 16f
        impact = true
        keepVelocity = false
        collides = false
        pierce = true
        hittable = false
        absorbable = false
        pierceArmor = true
    }

    @JvmField var damageInterval = 5f
    override fun continuousDamage() = if (continuous) damage / damageInterval * 60f else -1f
    override fun estimateDPS() = if (continuous) damage * 100f / damageInterval * 3f else super.estimateDPS()
    override fun calculateRange() = length.coerceAtLeast(maxRange)
    override fun init() {
        super.init()
        if (hitColor == Color.white)
            hitColor = fieldColor.cpy().a(1f)
        drawSize = drawSize.coerceAtLeast(length * 2f)
    }

    override fun init(b: Bullet?) {
        super.init(b)
        if (!continuous) {
            //applyDamage()
        }
    }

    override fun draw(b: Bullet) = b.run {
        val curLen = currentLength(this)
        Lines.stroke(curLen)
        Draw.color(fieldColor)
        Draw.alpha(0.5f + G.sin) // breath
        //Draw.alpha(fieldAlpha)
        Lines.arc(x, y, curLen / 2f, angle / 360f, this.rotation() - angle / 2f)
        Draw.reset()
    }

    fun applyDamage(b: Bullet) {
        Damage.collideLine(b, b.team, hitEffect, b.x, b.y, b.rotation(), currentLength(b), true, laserAbsorb, pierceCap)
    }

    fun currentLength(b: Bullet): Float {
        return length * b.fin(lengthInterp)
    }
}