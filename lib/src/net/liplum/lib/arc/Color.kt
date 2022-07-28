package net.liplum.lib.arc

import arc.graphics.Color
import arc.util.Time
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

enum class LerpType {
    RGB, HSV
}

fun Color(hex: String): Color = Color.valueOf(hex)
fun Color.darken(percentage: Float): Color {
    r *= 1f - percentage
    g *= 1f - percentage
    b *= 1f - percentage
    return this
}

fun Color.lighten(strength: Float): Color {
    r *= 1f - strength
    g *= 1f - strength
    b *= 1f - strength
    r += strength
    g += strength
    b += strength
    return this
}

class AnimatedColor(
    val colorSeq: Array<Color>,
    val duration: Float = 60f,
    val lerp: LerpType = LerpType.RGB,
    val useGlobalTime: Boolean = false,
) {
    var time = 0f
        set(value) {
            field = value.coerceAtLeast(0f)
        }

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
            val time = if (useGlobalTime) Time.globalTime else time
            val count = (time / duration).toInt() % colorSeq.size
            val curIndex = count.coerceIn(0, colorSeq.size - 1)
            val nextIndex = if (curIndex == colorSeq.size - 1) 0 else curIndex + 1
            val progress = (time % duration) / duration
            when (lerp) {
                LerpType.RGB -> field.set(colorSeq[curIndex]).lerp(colorSeq[nextIndex], progress)
                LerpType.HSV -> field.set(colorSeq[curIndex]).hsvLerp(colorSeq[nextIndex], progress)
            }
            return field
        }
}