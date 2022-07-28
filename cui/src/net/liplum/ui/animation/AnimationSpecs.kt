package net.liplum.ui.animation

import arc.math.Interp
import net.liplum.lib.arc.invoke

class WrapAnimationSpec(
    val interp: Interp,
) : AnimationSpec {
    override fun decorate(progress: Float): Float =
        interp(progress)
}

class FadeAnimationSpec : AnimationSpec {
    override fun decorate(progress: Float): Float =
        Interp.fade(progress)
}
class SmoothAnimationSpec : AnimationSpec {
    override fun decorate(progress: Float): Float =
        Interp.smooth(progress)
}