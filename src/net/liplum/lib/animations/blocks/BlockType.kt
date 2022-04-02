package net.liplum.lib.animations.blocks

import mindustry.gen.Building
import mindustry.world.Block

enum class ShareMode {
    UseMain, KeepSelf
}

open class BlockType<TBlock : Block, TBuild : Building> {
    companion object {
        @JvmStatic
        fun <TBlock : Block, TBuild : Building> byObj(
            shareMode: ShareMode = ShareMode.KeepSelf,
            isMain: Boolean = false,
            objGen: BlockType<TBlock, TBuild>.(TBlock, TBuild) -> BlockObj<TBlock, TBuild>
        ): BlockType<TBlock, TBuild> {
            return BlockType<TBlock, TBuild>().apply {
                this.shareMode = shareMode
                this.isMain = isMain
                this.objGen = objGen
            }
        }
        @JvmStatic
        fun <TBlock : Block, TBuild : Building> render(
            shareMode: ShareMode = ShareMode.KeepSelf,
            isMain: Boolean = false,
            render: BlockObj<TBlock, TBuild>.(TBlock, TBuild) -> Unit
        ): BlockType<TBlock, TBuild> {
            return BlockType<TBlock, TBuild>().apply {
                this.shareMode = shareMode
                this.isMain = isMain
                this.render = render
            }
        }
    }

    var shareMode: ShareMode = ShareMode.UseMain
    var render: (BlockObj<TBlock, TBuild>.(TBlock, TBuild) -> Unit)? = null
    var objGen: (BlockType<TBlock, TBuild>.(TBlock, TBuild) -> BlockObj<TBlock, TBuild>)? = null
    var isMain: Boolean = false
    var zIndex: Int = 0
    open fun newObj(block: TBlock, build: TBuild): BlockObj<TBlock, TBuild> {
        return if (objGen != null) {
            objGen!!.invoke(this, block, build)
        } else {
            BlockObj(block, build, this)
        }
    }
}