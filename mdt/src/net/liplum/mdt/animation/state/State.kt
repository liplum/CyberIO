package net.liplum.mdt.animation.state

import net.liplum.mdt.animation.ContextDraw

/**
 * The animation state which decide how to render.
 */
class State<T>(
    val stateName: String,
    /**
     * Use [ContextDraw] to obtain [StateConfig.transition] effect.
     */
    var renderer: T.() -> Unit = {},
) {
    /**
     * Renders the current image
     *
     * @param entity the subject to be rendered
     */
    fun draw(entity: T) {
        renderer(entity)
    }

    override fun hashCode() = stateName.hashCode()
    override fun toString() = stateName
    override fun equals(other: Any?): Boolean {
        return when (other) {
            is State<*> -> other.stateName == this.stateName
            is String -> other == this.stateName
            else -> false
        }
    }
}