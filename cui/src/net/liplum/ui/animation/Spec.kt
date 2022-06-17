package net.liplum.ui.animation

interface AnimationSpec {
    fun decorate(progress: Float): Float
    @Suppress("UNCHECKED_CAST")
    operator fun <T : AnimationSpec> plus(other: AnimationSpec): T =
        ComposedAnimationSpec(this, other) as T
}

class ComposedAnimationSpec(
    val a: AnimationSpec,
    val b: AnimationSpec,
) : AnimationSpec {
    override fun decorate(progress: Float): Float {
        return a.decorate(progress) * b.decorate(progress)
    }
}