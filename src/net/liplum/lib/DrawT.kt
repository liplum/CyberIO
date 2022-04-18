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

fun TR.DrawCuttingWidth(x: Float, y: Float, width: Float) {
}

fun ResetColor() {
    Draw.color()
}

fun Reset() {
    Draw.reset()
}

class Anchor {
    @JvmField var dx = 0f
    @JvmField var dy = 0f
    @JvmField var rotation = 0f
    /**
     * [dx] and [dy] will move the same length.
     */
    fun offset(d: Float): Anchor {
        dx += d
        dy += d
        return this
    }

    fun rotate(degree: Float = 90f): Anchor {
        rotation += degree
        return this
    }

    fun rotate90(times: Int): Anchor {
        rotation += times * 90f
        return this
    }

    fun reset() {
        dx = 0f
        dy = 0f
        rotation = 0f
    }
}
