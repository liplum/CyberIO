package net.liplum.lib.math

import arc.math.Interp
import arc.math.Mathf
import plumy.core.arc.invoke
import kotlin.math.min

fun Float.lerp(target: Float, progress: Float): Float =
    Mathf.lerp(this, target, progress)
/**
 * @return self
 */
fun FloatArray.lerp(target: FloatArray, progress: Float) = this.apply {
    for (i in 0 until min(target.size, target.size)) {
        this[i] = this[i].lerp(target[i], progress)
    }
}
/**
 * Return the smooth interpolation.
 * @receiver any number, finally is coerced in [0f,1f]
 * @return [0f,1f]
 */
val Progress.smooth: Progress
    get() = Interp.smooth(this.coerceIn(0f, 1f))
/**
 * Return the smoother interpolation.
 * @receiver any number, finally is coerced in [0f,1f]
 * @return [0f,1f]
 */
val Progress.smoother: Progress
    get() = Interp.smoother(this.coerceIn(0f, 1f))
/**
 * Return the slope interpolation.
 * @receiver any number, finally is coerced in [0f,1f] -> [1f,0f]
 * @return [0f,1f]
 */
val Progress.slope: Progress
    get() = Interp.slope(this.coerceIn(0f, 1f))
/**
 * Return the power2 interpolation.
 * @receiver any number, finally is coerced in [0f,1f]
 * @return [0f,1f]
 */
val Progress.pow2Intrp: Progress
    get() = Interp.pow2(this.coerceIn(0f, 1f))
/**
 * Return the power3 interpolation.
 * @receiver any number, finally is coerced in [0f,1f]
 * @return [0f,1f]
 */
val Progress.pow3Intrp: Progress
    get() = Interp.pow3(this.coerceIn(0f, 1f))
/**
 * Return the power2 Out interpolation.
 * @receiver any number, finally is coerced in [0f,1f]
 * @return [0f,1f]
 */
val Progress.pow2OutIntrp: Progress
    get() = Interp.pow2Out(this.coerceIn(0f, 1f))
/**
 * Return the power3 Out interpolation.
 * @receiver any number, finally is coerced in [0f,1f]
 * @return [0f,1f]
 */
val Progress.pow3OutIntrp: Progress
    get() = Interp.pow3Out(this.coerceIn(0f, 1f))
/**
 * Return the power2 In interpolation.
 * @receiver any number, finally is coerced in [0f,1f]
 * @return [0f,1f]
 */
val Progress.pow2InIntrp: Progress
    get() = Interp.pow2In(this.coerceIn(0f, 1f))
/**
 * Return the power3 In interpolation.
 * @receiver any number, finally is coerced in [0f,1f]
 * @return [0f,1f]
 */
val Progress.pow3InIntrp: Progress
    get() = Interp.pow3In(this.coerceIn(0f, 1f))
/**
 * @param growingTime the increasing number
 * @param maxTime the max bound
 * @return [0f,1f], increasing
 */
fun progressT(growingTime: Float, maxTime: Float): Float =
    1f - ((maxTime - growingTime) / maxTime).coerceIn(0f, 1f)
/**
 * Return 1f - this
 */
val Float.reverseProgress: Float
    get() = 1f - this

/**
 * @receiver max
 * @return current progress in [0,max)
 */
fun Int.progress(progress: Progress): Int {
    val p = progress.coerceIn(0f, 1f)
    return (p * this).toInt().coerceAtMost(this - 1)
}
/**
 * @receiver the progress in [0f,1f]
 * @param from this value
 * @param to to this value
 * @return a value from [from] to [to] in [progress]
 */
fun Progress.between(from: Float, to: Float): Float {
    if (from > to) {
        return from - (from - to) * this
    } else if (from < to) {
        return from + (to - from) * this
    }
    return from
}
