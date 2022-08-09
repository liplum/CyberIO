package net.liplum.statusFx

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Fill
import arc.math.Mathf
import mindustry.entities.Effect
import mindustry.type.StatusEffect
import net.liplum.common.shader.on
import net.liplum.registry.SD

val StaticFx = Effect(40f) {
    val x = it.x
    val y = it.y
    SD.TvStatic.on {
        Draw.color(Color.white)
        Fill.circle(
            x, y, Mathf.lerp(it.fslope(), 0.8f, 0.5f) * 5f
        )
    }
}

open class Static(name: String) : StatusEffect(name) {
    @JvmField var initTime = 10 * 60f

    init {
        damage = 0.2f
        effect = StaticFx
    }
}