@file:JvmName("DrawH")

package net.liplum.mdt.render

import mindustry.graphics.Drawf
import net.liplum.lib.TR

@JvmOverloads
fun TR.AsShadow(
    x: Float, y: Float, size: Float,
    rotation: Float = 0f
) {
    Drawf.shadow(
        this, x, y,
        G.Dw(this) * size,
        G.Dh(this) * size,
        rotation
    )
}