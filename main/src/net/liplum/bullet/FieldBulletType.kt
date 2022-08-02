package net.liplum.bullet

import arc.math.Interp
import mindustry.content.Fx
import mindustry.entities.Damage
import mindustry.entities.bullet.BulletType
import mindustry.gen.Bullet
import net.liplum.lib.math.Degree
import net.liplum.mdt.utils.WorldXY

class FieldBulletType : BulletType {
    constructor(speed: Float, damage: Float) : super(speed, damage)
    constructor() : super()

    @JvmField var continuous = true
    @JvmField var length: WorldXY = 220f
    @JvmField var angle: Degree = 60f
    @JvmField var lengthInterp: Interp = Interp.slope

    init {
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
    }

    @JvmField var damageInterval = 5f
    override fun continuousDamage() = if (continuous) damage / damageInterval * 60f else -1f
    override fun estimateDPS() = if (continuous) damage * 100f / damageInterval * 3f else super.estimateDPS()
    override fun calculateRange() = length.coerceAtLeast(maxRange)
    override fun init() {
        super.init()
        drawSize = drawSize.coerceAtLeast(length * 2f)
    }

    override fun init(b: Bullet?) {
        super.init(b)
        if (!continuous) {
            //applyDamage()
        }
    }



    fun applyDamage(b: Bullet) {
        Damage.collideLine(b, b.team, hitEffect, b.x, b.y, b.rotation(), currentLength(b), true, laserAbsorb, pierceCap)
    }

    fun currentLength(b: Bullet): Float {
        return length * b.fin(lengthInterp)
    }
}