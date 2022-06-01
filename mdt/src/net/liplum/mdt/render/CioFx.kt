package net.liplum.mdt.render

import arc.graphics.g2d.Draw
import mindustry.entities.Effect
import mindustry.graphics.Pal
import net.liplum.lib.TR

object CioFx {
    val upgrade = Effect(25f) {
        val data = it.data
        if(data !is TR) return@Effect

        Draw.color(Pal.accent)
        Draw.alpha(it.fout())
        Draw.rect(data, it.x, it.y)
    }
}