package net.liplum.inputs

import arc.Core
import mindustry.graphics.Drawf
import net.liplum.utils.drawXY

fun drawCircleOnMouse(radius: Float) {
    val mx = Core.input.mouseX().mouseXToTileX()
    val my = Core.input.mouseY().mouseXToTileY()
    Drawf.circles(mx.drawXY, my.drawXY, radius)
}

inline fun drawOnMouse(howToDraw: (Float, Float) -> Unit) {
    val mx = Core.input.mouseX().mouseXToTileX()
    val my = Core.input.mouseY().mouseXToTileY()
    howToDraw(mx.drawXY, my.drawXY)
}
