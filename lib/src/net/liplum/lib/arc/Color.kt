package net.liplum.lib.arc

import arc.graphics.Color
import net.liplum.lib.math.Progress
import net.liplum.lib.math.lerp

private val hsvTemp1 = FloatArray(3)
private val hsvTemp2 = FloatArray(3)
/**
 * @return self
 */
fun Color.hsvLerp(target: Color, progress: Progress) = this.apply {
    val hsvA = this.toHsv(hsvTemp1)
    val hsvB = target.toHsv(hsvTemp2)
    hsvA.lerp(hsvB, progress)
    this.fromHsv(hsvA)
}

fun String.tinted(color: Color) =
    "[#${color}]$this[]"

class AnimatedColor(
    val colorSeq: Array<Color>,
    val duration: Float = 120f,
) {
    var time = 0f
    fun spend(delta: Float) {
        time += delta
    }
    /**
     * Get the current color.
     *
     * NOTE: It's an expensive call, please cache the return value.
     */
    val color = Color()
        get() {
            val progress = (time % duration) / duration
            val curIndex = (progress * (colorSeq.size - 1)).toInt().coerceIn(0, colorSeq.size - 1)
            val nextIndex = if (curIndex == colorSeq.size - 1) 0 else curIndex + 1
            field.set(colorSeq[curIndex]).lerp(colorSeq[nextIndex], progress)
            return field
        }
}