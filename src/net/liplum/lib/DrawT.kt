@file:JvmName("DrawT")

package net.liplum.lib

import arc.graphics.Color
import arc.graphics.g2d.Draw
import mindustry.gen.Building
import net.liplum.utils.G
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
@JvmOverloads
fun TR.DrawSize(
    x: Float, y: Float, size: Float,
    rotation: Float = 0f
) {
    Draw.alpha(Draw.getColor().a * ALPHA)
    Draw.rect(this, x, y, G.Dw(this) * size, G.Dh(this) * size, rotation)
}
@JvmOverloads
fun TR.DrawOn(build: Building, rotation: Float = 0f) {
    this.Draw(build.x, build.y, rotation)
}
@JvmOverloads
fun TR.Draw(
    x: Float, y: Float, rotation: Float = 0f
) {
    Draw.alpha(Draw.getColor().a * ALPHA)
    Draw.rect(this, x, y, rotation)
}
@JvmOverloads
fun TR.DrawAny(
    x: Float, y: Float,
    width: Float = G.Dw(this), height: Float = G.Dh(this),
    rotation: Float = 0f
) {
    Draw.alpha(Draw.getColor().a * ALPHA)
    Draw.rect(this, x, y, width, height, rotation)
}

fun ResetColor() {
    Draw.color()
}

fun Reset() {
    Draw.reset()
}
