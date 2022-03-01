package net.liplum.animations.anims.blocks

import arc.util.Time
import mindustry.gen.Building
import net.liplum.animations.anims.IFrameIndexer

fun AutoAnimation.indexByTimeScale(tileEntity: Building): IFrameIndexer {
    return IFrameIndexer {
        if (it == 0) {
            return@IFrameIndexer -1
        }
        val fixedTotalDuration = totalDuration / tileEntity.timeScale
        val progress = Time.time % fixedTotalDuration / fixedTotalDuration//percent
        val index = (progress * it).toInt()
        return@IFrameIndexer index.coerceIn(0, it - 1)
    }
}