@file:JvmName("MathH")

package net.liplum.utils

import arc.math.Angles
import arc.math.Mathf
import arc.math.geom.Vec2
import kotlin.math.*

typealias FUNC = (Float) -> Float

/**
 * Generates a quadratic function whose domain is [0,1] and range is included in [0,1].
 * @param yWhenXIsZero the value when x=0
 */
fun quadratic(yWhenXIsZero: Float): FUNC {
    return quadratic(1f, yWhenXIsZero)
}
/**
 * Generates a quadratic function whose domain is [0,1] and range is included in [0,1].
 * @param yWhenXIs1 the value in [0,1] when x=1
 * @param yWhenXIsZero the value in [0,1] when x=0
 */
fun quadratic(yWhenXIs1: Float, yWhenXIsZero: Float): FUNC {
    val T = yWhenXIs1
    val c = yWhenXIsZero
    return {
        (c - T) * it * it + (2 * T - 2 * c) * it + c
    }
}
/**
 * Gets a certain function
 * @param func target function, no limit
 * @param minX the minimum of `func`'s domain
 * @param maxX the maximum of `func`'s domain
 * @return a new function g(x), x∈`[0..1]`, g(x)∈`[func(min)..func(max)]`
 */
fun rangeAdaptor(
    func: FUNC,
    minX: Float = 0f, maxX: Float
): FUNC {
    val d = abs(maxX - minX)
    return {
        val x = it.coerceIn(0f, 1f) * d + minX
        func(x)
    }
}
/**
 * Gets a certain function
 * @param func target function that has a reverse function
 * @param rfunc `func`'s reverse function
 * @param minY the minimum of `func`'s range
 * @param maxY the maximum of `func`'s range
 * @return a new function g(x), x∈`[0..1]`, g(x)∈`[ minY..maxY ]`
 */
fun shrink(
    func: FUNC, rfunc: FUNC,
    minY: Float, maxY: Float
): FUNC {
    val minYx = rfunc(minY)
    val maxYx = rfunc(maxY)
    val minX = min(minYx, maxYx)
    val maxX = max(minYx, maxYx)
    return rangeAdaptor(func, minX, maxX)
}

val Float.radian: Float
    get() = this * Mathf.degreesToRadians
val Float.degree: Float
    get() = this * Mathf.radiansToDegrees
val Power2: FUNC = {
    it * it
}
val Power3: FUNC = {
    it * it * it
}
val Log2: FUNC = Mathf::log2
val Log3: FUNC = {
    Mathf.log(3f, it)
}

fun PowerGen(power: Float): FUNC = {
    Mathf.pow(it, power)
}

fun LogGen(base: Float): FUNC = {
    Mathf.log(base, it)
}

fun ExpGen(base: Float): FUNC = {
    Mathf.pow(base, it)
}

fun ExpLogGen(base: Float): Pair<FUNC, FUNC> =
    Pair(ExpGen(base), LogGen(base))

val LogE: FUNC = ::ln
val ExpE: FUNC = ::exp
fun toAngle(x: Float, y: Float): Float =
    Angles.angle(x, y)

fun toAngle(x: Int, y: Int): Float =
    Angles.angle(x.toFloat(), y.toFloat())

fun toAngle(x1: Int, y1: Int, x2: Int, y2: Int): Float =
    Angles.angle(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat())

fun toAngle(x1: Float, y1: Float, x2: Float, y2: Float): Float =
    Angles.angle(x1, y1, x2, y2)

fun toAngle(x: Double, y: Double): Float =
    Angles.angle(x.toFloat(), y.toFloat())

fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    val x = x1 - x2
    val y = y1 - y2
    return Mathf.sqrt(x * x + y * y)
}

fun Vec2.normal(): Vec2 {
    this.set(y, -x)
    return this
}

fun Vec2.normal(factor: Float): Vec2 {
    this.set(factor * y, -factor * x)
    return this
}