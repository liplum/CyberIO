package net.liplum.math

import plumy.core.math.FUNC
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

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
@Suppress("UnnecessaryVariable")
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
    minX: Float = 0f, maxX: Float,
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
    minY: Float, maxY: Float,
): FUNC {
    val minYx = rfunc(minY)
    val maxYx = rfunc(maxY)
    val minX = min(minYx, maxYx)
    val maxX = max(minYx, maxYx)
    return rangeAdaptor(func, minX, maxX)
}
