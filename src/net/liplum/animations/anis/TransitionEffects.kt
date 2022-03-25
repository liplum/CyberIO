@file:JvmName("TransitionEffects")

package net.liplum.animations.anis

import arc.math.Mathf

val None = TransitionEffect { _, _, cur ->
    cur.run()
}
val Fade = TransitionEffect { progress, last, cur ->
    val p = Mathf.lerp(progress, progress * progress, 0.5f)
    val lastAlpha = 1f - p
    if (lastAlpha > 0) {
        ALPHA = lastAlpha
        last.run()
    }
    if (p > 0) {
        ALPHA = p
        cur.run()
    }
}
