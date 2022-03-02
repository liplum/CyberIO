package net.liplum.utils

import arc.math.Mathf
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
