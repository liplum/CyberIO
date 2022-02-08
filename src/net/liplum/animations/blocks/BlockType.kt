package net.liplum.animations.blocks

typealias BLOCK = mindustry.world.Block
typealias BUILD = mindustry.gen.Building

enum class ShareMode {
    UseMain, KeepSelf
}

open class BlockType<TBlock : BLOCK, TBuild : BUILD>(
    var shareMode: ShareMode = ShareMode.UseMain,
    var isMain: Boolean = false
) {
    var zIndex: Int = 0
    open fun newObj(block: TBlock, build: TBuild): BlockObj<TBlock, TBuild> {
        return BlockObj(block, build, this)
    }
}