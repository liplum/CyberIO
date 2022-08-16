package net.liplum.mdt.animation.state

/**
 * The animation state which decide how to render
 */
class State<T>(
    val stateName: String,
    var renderer: (T.() -> Unit)? = null,
) {
    var isOverwriteBlock = false
        private set
    /**
     * Renders the current image
     *
     * @param build the subject to be rendered
     */
    fun drawBuilding(build: T) {
        renderer?.invoke(build)
    }

    fun setOverwriteBlock(overwriteBlock: Boolean): State<T> {
        isOverwriteBlock = overwriteBlock
        return this
    }

    fun setRenderer(renderer: T.() -> Unit): State<T> {
        this.renderer = renderer
        return this
    }

    override fun hashCode() =
        stateName.hashCode()

    override fun toString() =
        stateName

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is State<*> -> other.stateName == this.stateName
            is String -> other == this.stateName
            else -> false
        }
    }
}