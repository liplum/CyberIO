@file:JvmName("FxH")

package net.liplum.utils

import arc.math.Interp
import mindustry.entities.Effect
import mindustry.entities.Effect.EffectContainer
import plumy.core.math.invoke

/**
 * @param duration the duration of fade-in&out
 */
fun EffectContainer.fadeInOut(
    duration: Float = lifetime * 0.1f,
): Float =
    // Fade in:
    if (time < duration)
        Interp.fade(time / duration)
    // Fade out
    else if (lifetime - time < duration)
        Interp.fade((lifetime - time) / duration)
    // Appear clearly
    else
        1f
/**
 * @param durationPct final fade-in&out duration = toast.duration * durationPct
 */
fun EffectContainer.fadeInOutPct(
    durationPct: Float = 0.1f,
): Float {
    val duration = durationPct * lifetime
    // Fade in:
    return if (time < duration)
        Interp.fade(time / duration)
    // Fade out
    else if (lifetime - time < duration)
        Interp.fade((lifetime - time) / duration)
    // Appear clearly
    else
        1f
}

fun Effect.atUnit(unit: MUnit) {
    at(unit.x, unit.y, unit.rotation, unit)
}