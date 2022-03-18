package net.liplum.seffects

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Fill
import arc.math.Mathf
import mindustry.entities.Effect
import mindustry.type.StatusEffect
import net.liplum.shaders.SD
import net.liplum.shaders.on

val StaticFx = Effect(40f) {
    SD.tvSnow.on {
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
    init {
        damage = 5f
        effect = StaticFx
    }
}