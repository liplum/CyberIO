package net.liplum.lib.arc

import arc.graphics.Color
import net.liplum.lib.math.lerp

private val hsvTemp1 = FloatArray(3)
private val hsvTemp2 = FloatArray(3)
/**
 * @return self
 */
fun Color.hsvLerp(target: Color, progress: Float) = this.apply {
    val hsvA = this.toHsv(hsvTemp1)
    val hsvB = target.toHsv(hsvTemp2)
    hsvA.lerp(hsvB, progress)
    this.fromHsv(hsvA)
}

fun String.tinted(color: Color) =
    "[#${color}]$this[]"
