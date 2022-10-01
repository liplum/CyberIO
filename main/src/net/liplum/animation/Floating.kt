package net.liplum.animation

import arc.math.Mathf
import arc.math.geom.Vec2
import plumy.core.math.Polar
import plumy.core.math.nextBoolean

open class Floating(
    val minR: Float,
    val maxR: Float,
    var clockwise: Boolean = false,
) {
    val pos = Polar()
    val x: Float
        get() = pos.x
    val y: Float
        get() = pos.y

    constructor(range: Float) : this(0f, range)

    @JvmField var rAdding = false
    var changeRate = 0
    open fun randomPos(): Floating {
        pos.r = Mathf.random(minR, maxR)
        pos.a = Mathf.random(0f, 2 * Mathf.PI)
        rAdding = nextBoolean()
        return this
    }

    open fun move(delta: Float) {
        if (changeRate > 0 && Mathf.random(99) < changeRate) {
            rAdding = !rAdding
        }
        if (rAdding)
            pos.r += delta
        else
            pos.r -= delta
        if (pos.r >= maxR) {
            rAdding = false
        } else if (pos.r <= minR) {
            rAdding = true
        }
        if (clockwise)
            pos.a -= delta
        else
            pos.a += delta
    }
}

fun Vec2.add(floating: Floating): Vec2 =
    this.add(floating.x, floating.y)

fun Vec2.sub(floating: Floating): Vec2 =
    this.sub(floating.x, floating.y)

operator fun Vec2.plusAssign(floating: Floating) {
    this.add(floating.x, floating.y)
}

operator fun Vec2.minusAssign(floating: Floating) {
    this.sub(floating.x, floating.y)
}
