package net.liplum.animations.blocks

import mindustry.gen.Building
import mindustry.world.Block

open class BlockGroupObj<TBlock : Block, TBuild : Building>(
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

    open fun spend(time: Float) {
        blockObjs.forEach { it.spend(time) }
    }

    open fun drawBuilding() {
        blockObjs.forEach {
            if (it.isMain) {
                it.drawBuild()
                lastMainXOffset = it.xOffset
                lastMainYOffset = it.yOffset
            } else {
                if (it.shareMode == ShareMode.UseMain) {
                    it.xOffset = lastMainXOffset
                    it.yOffset = lastMainYOffset
                }
                it.drawBuild()
            }
        }
    }
}