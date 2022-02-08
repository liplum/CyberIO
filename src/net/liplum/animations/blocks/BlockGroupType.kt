package net.liplum.animations.blocks

open class BlockGroupType<TBlock : BLOCK, TBuild : BUILD>(
    vararg initBlocks: BlockType<TBlock, TBuild>
) {
    var blocks: List<BlockType<TBlock, TBuild>>
    var mainBlock: BlockType<TBlock, TBuild>?

    init {
        blocks = initBlocks.sortedBy { it.zIndex }
        mainBlock = initBlocks.find { it.isMain } ?: blocks.firstOrNull()
        mainBlock?.isMain = true
    }

    open fun newObj(block: TBlock, build: TBuild): BlockGroupObj<TBlock, TBuild> {
        return BlockGroupObj(block, build, this)
    }
}