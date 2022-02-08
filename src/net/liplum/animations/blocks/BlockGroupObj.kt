package net.liplum.animations.blocks

open class BlockGroupObj<TBlock : BLOCK, TBuild : BUILD>(
    val block: TBlock,
    val build: TBuild,
    val prototype: BlockGroupType<TBlock, TBuild>
) {
    var blockObjs: List<BlockObj<TBlock, TBuild>> =
        prototype.blocks.map { it.newObj(block, build) }
    var lastMainXOffset = 0f
    var lastMainYOffset = 0f
    open fun update() {
        blockObjs.forEach {
            it.update()
        }
    }

    open fun drawBuilding() {
        blockObjs.forEach {
            if (it.isMain) {
                it.drawBuilding()
                lastMainXOffset = it.xOffset
                lastMainYOffset = it.yOffset
            } else {
                if (it.shareMode == ShareMode.UseMain) {
                    it.xOffset = lastMainXOffset
                    it.yOffset = lastMainYOffset
                }
                it.drawBuilding()
            }
        }
    }
}