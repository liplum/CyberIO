@file:JvmName("MathH")

package net.liplum.utils

import arc.math.Angles
import arc.math.Mathf
import arc.math.geom.Vec2
import net.liplum.draw
import net.liplum.math.Polar
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
/**
 * @return this
 */
fun Vec2.normal(): Vec2 {
    this.set(y, -x)
    return this
}
/**
 * @return this
 */
fun Vec2.normal(factor: Float): Vec2 {
    this.set(factor * y, -factor * x)
    return this
}
/**
 * @param targe degree of target angle
 * @param speed the rotation speed
 * @return this
 */
fun Vec2.approachAngle(targe: Float, speed: Float): Vec2 =
    setAngle(
        Angles.moveToward(
            angle(),
            targe,
            speed
        )
    )
/**
 * @param targeX X of target position
 * @param targeY Y of target position
 * @param speed the rotation speed
 * @return this
 */
fun Vec2.approachAngle(targeX: Float, targeY: Float, speed: Float): Vec2 =
    setAngle(
        Angles.moveToward(
            angle(),
            Angles.angle(x, y, targeX, targeY),
            speed
        )
    )

fun Vec2.approachLen(targetLen: Float, speed: Float): Vec2 =
    setLength(
        Mathf.approach(len(), targetLen, speed)
    )

fun Polar.approachR(targetR: Float, speed: Float): Polar {
    r = Mathf.approach(r, targetR, speed)
    return this
}
/**
 * @param targetDegree an angle in degree
 */
fun Polar.approachADegree(targetDegree: Float, speed: Float): Polar {
    a = Angles.moveToward(a.degree, targetDegree, speed).radian
    return this
}
/**
 * @param targetRadian an angle in radian
 */
fun Polar.approachA(targetRadian: Float, speed: Float): Polar {
    a = Angles.moveToward(a.degree, targetRadian.draw, speed).radian
    return this
}
/**
 * Roll a number belongs to `[0,this)`
 * @return the result that doesn't equal to [exception]. Otherwise, -1 will be returned.
 */
fun Int.randomExcept(exception: Int): Int {
    var res: Int
    when (val len = this.coerceAtLeast(0)) {
        0 -> res = -1
        1 -> res = 0
        else -> do {
            res = Mathf.random(0, len - 1)
        } while (res == exception)
    }
    return res
}
/**
 * Random select an element by its weight
 * @param weights it.size == list.size.
 */
fun <T> List<T>.randomByWeights(
    weights: Array<Int>,
    maxWeight: Int = weights.last()
): T {
    assert(weights.size == size) { "Weights' size(${weights.size}) don't match receiver's size($size)" }
    var pos = Mathf.random(maxWeight - 1)
    for (i in indices) {
        val weight = weights[i]
        if (pos < weight) {
            return this[i]
        }
        pos -= weights[i]
    }
    throw ArithmeticException("Random weight($pos) is over than maximum($maxWeight)")
}

fun Vec2.set(x: Short, y: Short): Vec2 =
    this.set(x.toFloat(), y.toFloat())

fun Vec2.set(x: Int, y: Int): Vec2 =
    this.set(x.toFloat(), y.toFloat())

fun Vec2.minus(x: Int, y: Int): Vec2 = apply {
    this.x -= x.toFloat()
    this.y -= y.toFloat()
}

fun Vec2.minus(x: Short, y: Short): Vec2 = apply {
    this.x -= x.toFloat()
    this.y -= y.toFloat()
}

fun Vec2.minus(x: Float, y: Float): Vec2 = apply {
    this.x -= x
    this.y -= y
}
private val temp = Vec2()
fun isDiagonalTo(
    x1: Float, y1: Float, x2: Float, y2: Float,
): Boolean {
    return (temp.set(x1, y1).minus(x2, y2).angle() % 45f).isZero
}
fun isDiagonalTo(
    x1: Int, y1: Int, x2: Int, y2: Int,
): Boolean {
    return (temp.set(x1, y1).minus(x2, y2).angle() % 45f).isZero
}
