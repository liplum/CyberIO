package net.liplum.mdt.animations

import arc.math.Mathf
import net.liplum.lib.math.Polar

open class Floating(
    val minR: Float,
    val maxR: Float,
) {
    val pos = Polar()
    val x: Float
        get() = pos.x
    val y: Float
        get() = pos.y

    constructor(range: Float) : this(0f, range)

    @JvmField var rAdding = false
    @JvmField var aAdding = false
    var changeRate = 0
    open fun randomPos(): Floating {
        pos.r = Mathf.random(minR, maxR)
        pos.a = Mathf.random(0f, 2 * Mathf.PI)
        rAdding = Mathf.randomBoolean()
        aAdding = Mathf.randomBoolean()
        return this
    }

    open fun move(delta: Float) {
        if (changeRate > 0 && Mathf.random(99) < changeRate) {
            aAdding = !aAdding
        }
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
        if (aAdding)
            pos.a += delta
        else
            pos.a -= delta
    }
}