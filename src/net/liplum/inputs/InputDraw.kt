package net.liplum.inputs

import mindustry.graphics.Drawf
import net.liplum.utils.drawXY

fun drawCircleOnMouse(radius: Float) {
    val mx = Screen.tileXOnMouse()
    val my = Screen.tileYOnMouse()
    Drawf.circles(mx.drawXY, my.drawXY, radius)
}

inline fun drawOnMouse(howToDraw: (Float, Float) -> Unit) {
    val mx = Screen.tileXOnMouse()
    val my = Screen.tileYOnMouse()
    howToDraw(mx.drawXY, my.drawXY)
}
