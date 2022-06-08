@file:JvmName("DrawH")

package net.liplum.mdt.render

import mindustry.graphics.Drawf
import net.liplum.lib.TR
import net.liplum.mdt.render.G.realHeight
import net.liplum.mdt.render.G.realWidth

@JvmOverloads
fun TR.AsShadow(
    x: Float, y: Float, size: Float,
    rotation: Float = 0f
) {
    Drawf.shadow(
        this, x, y,
        this.realWidth * size,
        this.realHeight * size,
        rotation
    )
}