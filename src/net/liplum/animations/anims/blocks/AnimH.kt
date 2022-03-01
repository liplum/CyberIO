package net.liplum.animations.anims.blocks

import arc.util.Time
import mindustry.gen.Building
import mindustry.world.blocks.defense.turrets.Turret
import net.liplum.animations.anims.IFrameIndexer

fun AutoAnimation.ixByTimeScale(tileEntity: Building) = IFrameIndexer {
    val fixedTotalDuration = totalDuration / tileEntity.timeScale
    val progress = Time.time % fixedTotalDuration / fixedTotalDuration//percent
    val index = (progress * it).toInt()
    return@IFrameIndexer index.coerceIn(0, it - 1)
}

fun AutoAnimation.ixByShooting(turret: Turret.TurretBuild, speedUpPct: Float = 2f) = IFrameIndexer {
    var fixedTotalDuration = totalDuration / turret.timeScale
    if (turret.wasShooting) {
        fixedTotalDuration /= speedUpPct
    }
    val progress = Time.time % fixedTotalDuration / fixedTotalDuration//percent
    val index = (progress * it).toInt()
    return@IFrameIndexer index.coerceIn(0, it - 1)
}

fun AutoAnimation.ixReciprocate(tileEntity: Building? = null) =
    if (tileEntity != null) {
        IFrameIndexer {
            val endI = it - 1
            val fixedTotalDuration = totalDuration * 2f / tileEntity.timeScale
            val progress = Time.time % fixedTotalDuration / fixedTotalDuration//percent
            var index = (progress * it * 2).toInt()
            if (index > endI) {
                index -= (index - endI) * 2
            }
            return@IFrameIndexer index.coerceIn(0, it - 1)
        }
    } else {
        IFrameIndexer {
            val endI = it - 1
            val fixedTotalDuration = totalDuration * 2f
            val progress = Time.time % fixedTotalDuration / fixedTotalDuration//percent
            var index = (progress * it * 2).toInt()
            if (index > endI) {
                index -= (index - endI) * 2
            }
            return@IFrameIndexer index.coerceIn(0, endI)
        }
    }

fun AutoAnimation.reciprocate() =
    this.indexer(ixReciprocate())
