package net.liplum.seffects

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Fill
import arc.math.Mathf
import mindustry.entities.Effect
import mindustry.gen.Unit
import mindustry.type.StatusEffect
import net.liplum.lib.shaders.on
import net.liplum.registries.SD

val StaticFx = Effect(40f) {
    SD.TvStatic.on {
        Draw.color(Color.white)
        Fill.circle(
            it.x, it.y,
            Mathf.lerp(it.fslope(), 0.8f, 0.5f)
                    * 5f
        )
    }
}

open class Static(name: String) : StatusEffect(name) {
    @JvmField var initTime = 10 * 60f
    @JvmField var seekRange = 60f

    init {
        damage = 0.2f
        effect = StaticFx
    }

    override fun update(unit: Unit, time: Float) {
        super.update(unit, time)
        /*val v = unit.vel
        val len = v.len()
        var nearestEnemy: Unit? = null
        var nearestDis = seekRange
        Groups.unit.intersect(
            unit.x - seekRange,
            unit.y - seekRange,
            seekRange * 2,
            seekRange * 2,
        ) {
            if (it.team != unit.team && it != unit) {
                val dst = it.dst2(unit)
                if (dst < nearestDis) {
                    nearestDis = dst
                    nearestEnemy = it
                }
            }
        }
        nearestEnemy?.let {
            val vec = Tmp.v1.set(it.x, it.y).sub(unit.x, unit.y)
            v.set(-vec.x, -vec.y).setLength(len)
        }*/
    }
}