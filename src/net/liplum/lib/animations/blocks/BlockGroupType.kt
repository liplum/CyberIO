package net.liplum.lib.animations.blocks

import mindustry.gen.Building
import mindustry.world.Block

open class BlockGroupType<TBlock : Block, TBuild : Building> {
    constructor(
        vararg initBlocks: BlockType<TBlock, TBuild>
    ) {
        blocks = initBlocks.sortedBy { it.zIndex }
        mainBlock = initBlocks.find { it.isMain } ?: blocks.firstOrNull()
        mainBlock?.isMain = true
    }

    constructor(
        gen: BlockGroupType<TBlock, TBuild>.() -> Unit
    ) {
        gen()
        blocks = waitingQueue!!.sortedBy { it.zIndex }
        mainBlock = waitingQueue!!.find { it.isMain } ?: blocks.firstOrNull()
        mainBlock?.isMain = true
        waitingQueue = null
    }

    var blocks: List<BlockType<TBlock, TBuild>>
    var mainBlock: BlockType<TBlock, TBuild>?
    var waitingQueue: ArrayList<BlockType<TBlock, TBuild>>? = null
    fun BlockType<TBlock, TBuild>.add(): BlockType<TBlock, TBuild> {
        if (waitingQueue == null) {
            waitingQueue = ArrayList()
        }
        waitingQueue!!.add(this)
        return this
    }

    fun addType(type: BlockType<TBlock, TBuild>): BlockType<TBlock, TBuild> {
        if (waitingQueue == null) {
            waitingQueue = ArrayList()
        }
        waitingQueue!!.add(type)
        return type
    }

    open fun newObj(block: TBlock, build: TBuild): BlockGroupObj<TBlock, TBuild> {
        return BlockGroupObj(block, build, this)
    }
}