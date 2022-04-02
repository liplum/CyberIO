@file:JvmName("DrawT")

package net.liplum.lib.animations.anis

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

fun DrawTR(tr: TR, x: Float, y: Float) {
    Draw.alpha(Draw.getColor().a * ALPHA)
    Draw.rect(tr, x, y)
}

fun DrawTrSize(tr: TR, x: Float, y: Float, size: Float) {
    Draw.alpha(Draw.getColor().a * ALPHA)
    Draw.rect(tr, x, y, G.Dw(tr) * size, G.Dh(tr) * size)
}

fun TR.DrawSize(x: Float, y: Float, size: Float) {
    Draw.alpha(Draw.getColor().a * ALPHA)
    Draw.rect(this, x, y, G.Dw(this) * size, G.Dh(this) * size)
}

fun DrawTrSizeOn(tr: TR, build: Building, size: Float) {
    Draw.alpha(Draw.getColor().a * ALPHA)
    Draw.rect(tr, build.x, build.y, G.Dw(tr) * size, G.Dh(tr) * size)
}

fun TR.DrawSizeOn(build: Building, size: Float) {
    DrawTrSizeOn(this, build, size)
}

fun DrawTrWH(tr: TR, x: Float, y: Float, width: Float, height: Float) {
    Draw.alpha(Draw.getColor().a * ALPHA)
    Draw.rect(tr, x, y, width, height)
}

fun TR.Draw(x: Float, y: Float) {
    DrawTR(this, x, y)
}
@JvmOverloads
fun DrawTrOn(tr: TR, build: Building, rotate: Boolean = false) {
    Draw.alpha(Draw.getColor().a * ALPHA)
    if (rotate) {
        Draw.rect(tr, build.x, build.y, build.rotation - 90f)
    } else {
        Draw.rect(tr, build.x, build.y)
    }
}
@JvmOverloads
fun TR.DrawOn(build: Building, rotate: Boolean = false) {
    DrawTrOn(this, build, rotate)
}

fun DrawRotatedTr(tr: TR, build: Building, rotation: Float) {
    DrawTR(tr, build.x, build.y, rotation)
}

fun TR.DrawRotateOn(build: Building, rotation: Float) {
    DrawRotatedTr(this, build, rotation)
}

fun DrawTR(tr: TR, x: Float, y: Float, rotation: Float) {
    Draw.alpha(Draw.getColor().a * ALPHA)
    Draw.rect(tr, x, y, rotation)
}

fun TR.Draw(x: Float, y: Float, rotation: Float) {
    DrawTR(this, x, y, rotation)
}

fun TR.DrawCuttingWidth(x: Float, y: Float,width: Float) {

}

fun ResetColor() {
    Draw.color()
}

fun Reset() {
    Draw.reset()
}