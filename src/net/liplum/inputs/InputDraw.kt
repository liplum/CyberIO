package net.liplum.inputs

import mindustry.graphics.Drawf
import net.liplum.utils.drawXY

fun drawCircleOnMouse(radius: Float) {
    val mx = tileXOnMouse()
    val my = tileYOnMouse()
    Drawf.circles(mx.drawXY, my.drawXY, radius)
}

inline fun drawOnMouse(howToDraw: (Float, Float) -> Unit) {
    val mx = tileXOnMouse()
    val my = tileYOnMouse()
    howToDraw(mx.drawXY, my.drawXY)
}
