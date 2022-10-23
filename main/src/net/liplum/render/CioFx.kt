package net.liplum.render

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Fill
import mindustry.graphics.Layer
import mindustry.graphics.Pal
import net.liplum.Var.ParticleEffectNumber
import net.liplum.Var.Rand
import plumy.core.assets.TR
import net.liplum.common.math.PolarX
import plumy.core.math.random
import net.liplum.render.G
import plumy.dsl.NewEffect

object CioFx {
    val upgrade = NewEffect(25f) {
        val data = data
        if (data !is TR) return@NewEffect

        Draw.color(Pal.accent)
        Draw.alpha(fout())
        Draw.rect(data, x, y)
    }
    val tempPolar = PolarX()
    val blackHoleAbsorbing = NewEffect(80f) {
        Draw.z(Layer.bullet - 1f)
        val range = rotation
        Draw.color(Color.black)
        Rand.setSeed(id.toLong())
        for (i in 0 until ParticleEffectNumber) {
            tempPolar.random(range, Rand).apply {
                r *= foutpow()
            }
            val size = Rand.random(0.2f, 3f)
            Fill.circle(tempPolar.x + x, tempPolar.y + y, size + G.sin / 3f)
        }
    }
}