@file:JvmName("DrawH")

package net.liplum.render

import mindustry.graphics.Drawf
import plumy.core.assets.TR
import net.liplum.render.G.realHeight
import net.liplum.render.G.realWidth

fun TR.AsShadow(
    x: Float, y: Float, size: Float = 1f,
    rotation: Float = 0f,
) {
    Drawf.shadow(
        this, x, y,
        this.realWidth * size,
        this.realHeight * size,
        rotation
    )
}