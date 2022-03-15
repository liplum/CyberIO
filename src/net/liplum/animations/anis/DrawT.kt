@file:JvmName("DrawT")

package net.liplum.animations.anis

import arc.graphics.Color
import arc.graphics.g2d.Draw
import net.liplum.utils.TR

var ALPHA: Float = 1f
    set(value) {
        field = value.coerceIn(0f, 1f)
    }

fun RESET_CONTEXT() {
    ALPHA = 1f
}

fun SetColor(color: Color) {
    Draw.color(color)
}

fun SetAlpha(alpha: Float) {
    Draw.alpha(alpha)
}

fun DrawTR(tr: TR, x: Float, y: Float) {
    Draw.alpha(Draw.getColor().a * ALPHA)
    Draw.rect(tr, x, y)
}

fun DrawTR(tr: TR, x: Float, y: Float, rotation: Float) {
    Draw.alpha(Draw.getColor().a * ALPHA)
    Draw.rect(tr, x, y, rotation)
}

fun ResetColor() {
    Draw.color()
}
fun Reset(){
    Draw.reset()
}