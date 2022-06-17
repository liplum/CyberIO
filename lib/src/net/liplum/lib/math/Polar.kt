package net.liplum.lib.math

import arc.math.Angles
import arc.math.Mathf
import arc.math.Rand
import arc.math.geom.Vec2
import arc.math.geom.Vec3
import kotlin.math.atan2

/**
 * It represents a polar coordinate using radian.
 */
open class Polar(
    @JvmField
    var r: Float = 0f,
    @JvmField
    var a: Float = 0f,
) {
    fun fromXY(x: Float, y: Float): Polar {
        r = Mathf.sqrt(x * x + y * y)
        a = atan2(y.toDouble(), x.toDouble()).toFloat()
        return this
    }

    fun fromV2d(v2d: Vec2): Polar {
        fromXY(v2d.x, v2d.y)
        return this
    }

    var angle: Float
        get() = a * Mathf.radiansToDegrees
        set(value) {
            a = (value % 360f) * Mathf.degreesToRadians
        }
    val x: Float
        get() = r * Mathf.cos(a)
    val y: Float
        get() = r * Mathf.sin(a)
    val v2d: Vec2
        get() = Vec2(x, y)
    val v3d: Vec3
        get() = Vec3(x.toDouble(), y.toDouble(), 1.0)

    companion object {
        @JvmStatic
        fun toR(x: Float, y: Float): Float {
            return Mathf.sqrt(x * x + y * y)
        }
        @JvmStatic
        fun toA(x: Float, y: Float): Float {
            return atan2(y.toDouble(), x.toDouble()).toFloat()
        }
        @JvmStatic
        fun byXY(x: Float, y: Float): Polar {
            return Polar().fromXY(x, y)
        }
        @JvmStatic
        fun byV2d(v2d: Vec2): Polar {
            return Polar().fromV2d(v2d)
        }
    }
}

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
    a = Angles.moveToward(a.degree, targetRadian, speed).radian
    return this
}
/**
 * @return self
 */
fun Polar.random(radiusRange: Float, random: Rand = Mathf.rand): Polar {
    angle = random.random(360f)
    r = random.random(0f, radiusRange)
    return this
}
