package net.liplum.mdt.animations.anis

import arc.util.Nullable
import mindustry.gen.Building
import mindustry.world.Block
import java.util.function.Consumer

/**
 * The animation state which decide how to render
 *
 * @param <TBlock> the type of block which has this animation state
 * @param <TBuild> the corresponding [Building] type
</TBuild></TBlock> */
class AniState<TBlock : Block, TBuild : Building> {
    /**
     * Gets the name
     *
     * @return name
     */
    val stateName: String
    @Nullable
    var renderer: (TBuild.() -> Unit)?
        private set
    var isOverwriteBlock = false
        private set
    /**
     * @param stateName a name
     */
    constructor(stateName: String) {
        this.stateName = stateName
        renderer = null
    }
    /**
     * @param stateName a name
     * @param renderer  how to render
     */
    constructor(stateName: String, renderer: TBuild.() -> Unit) {
        this.stateName = stateName
        this.renderer = renderer
    }
    /**
     * @param stateName a name
     * @param renderer  how to render
     * For Java
     */
    constructor(stateName: String, renderer: Consumer<TBuild>) {
        this.stateName = stateName
        this.renderer = { renderer.accept(this) }
    }
    /**
     * Renders the current image
     *
     * @param build the subject to be rendered
     */
    fun drawBuilding(build: TBuild) {
        renderer?.invoke(build)
    }

    fun setOverwriteBlock(overwriteBlock: Boolean): AniState<TBlock, TBuild> {
        isOverwriteBlock = overwriteBlock
        return this
    }

    fun setRenderer(renderer: TBuild.() -> Unit): AniState<TBlock, TBuild> {
        this.renderer = renderer
        return this
    }

    override fun hashCode() =
        stateName.hashCode()

    override fun toString() =
        stateName

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is AniState<*, *> -> other.stateName == this.stateName
            is String -> other == this.stateName
            else -> false
        }
    }
}