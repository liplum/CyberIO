package net.liplum.animations.blocks

open class BlockObj<TBlock : BLOCK, TBuild : BUILD>(
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

    open fun drawBuilding() {
    }
}