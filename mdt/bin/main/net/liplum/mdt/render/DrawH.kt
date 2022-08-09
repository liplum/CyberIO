@file:JvmName("DrawH")

package net.liplum.mdt.render

import mindustry.graphics.Drawf
import plumy.core.assets.TR
import net.liplum.mdt.render.G.realHeight
import net.liplum.mdt.render.G.realWidth

@JvmOverloads
fun TR.AsShadow(
    x: Float, y: Float, size: Float = 1f,
    rotation: Float = 0f
) {
    Drawf.shadow(
        this, x, y,
        this.realWidth * size,
        this.realHeight * size,
        rotation
    )
}