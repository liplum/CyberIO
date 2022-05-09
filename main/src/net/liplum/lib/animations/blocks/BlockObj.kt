package net.liplum.lib.animations.blocks

import mindustry.gen.Building
import mindustry.world.Block

open class BlockObj<TBlock : Block, TBuild : Building>(
    val block: TBlock,
    val build: TBuild,
    val prototype: BlockType<TBlock, TBuild>
) {
    val zIndex: Int
        get() = prototype.zIndex
    val isMain: Boolean
        get() = prototype.isMain
    val shareMode: ShareMode
        get() = prototype.shareMode
    var xOffset: Float = 0f
    var yOffset: Float = 0f
    open fun update() {
    }

    open fun spend(time: Float) {
    }

    open fun drawBuild() {
        prototype.render?.invoke(this, block, build)
    }
}