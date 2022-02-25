package net.liplum.utils

/**
 * Generates a quadratic function whose domain is [0,1] and range is included in [0,1].
 * @param yWhenXIsZero the value when x=0
 */
fun quadratic(yWhenXIsZero: Float): (Float) -> Float {
    return quadratic(1f, yWhenXIsZero)
}
/**
 * Generates a quadratic function whose domain is [0,1] and range is included in [0,1].
 * @param yWhenXIs1 the value in [0,1] when x=1
 * @param yWhenXIsZero the value in [0,1] when x=0
 */
fun quadratic(yWhenXIs1: Float, yWhenXIsZero: Float): (Float) -> Float {
    val T = yWhenXIs1
    val c = yWhenXIsZero
    return {
        (c - T) * it * it + (2 * T - 2 * c) * it + c
    }
}