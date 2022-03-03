@file:JvmName("AnimationH")

package net.liplum.animations.anims

import arc.graphics.g2d.TextureRegion
import arc.util.Time
import mindustry.gen.Building
import mindustry.world.blocks.defense.turrets.Turret

/**
 * It plays animation evenly based on [Time.time].
 */
@JvmOverloads
fun Animation.ixAuto(tileEntity: Building? = null) =
    if (tileEntity == null) IFrameIndexer {
        val progress = Time.time % duration / duration//percent
        val index = (progress * it).toInt()
        return@IFrameIndexer index.coerceIn(0, it - 1)
    } else IFrameIndexer {
        val fixedTotalDuration = duration / tileEntity.timeScale
        val progress = Time.time % fixedTotalDuration / fixedTotalDuration//percent
        val index = (progress * it).toInt()
        return@IFrameIndexer index.coerceIn(0, it - 1)
    }
@JvmOverloads
fun Animation.ixByShooting(turret: Turret.TurretBuild, speedUpPct: Float = 2f) = IFrameIndexer {
    var fixedTotalDuration = duration / turret.timeScale
    if (turret.wasShooting) {
        fixedTotalDuration /= speedUpPct
    }
    val progress = Time.time % fixedTotalDuration / fixedTotalDuration//percent
    val index = (progress * it).toInt()
    return@IFrameIndexer index.coerceIn(0, it - 1)
}
@JvmOverloads
fun Animation.ixSpeed(tileEntity: Building? = null, speedUpPct: () -> Float) =
    if (tileEntity != null) IFrameIndexer {
        var fixedTotalDuration = duration / tileEntity.timeScale
        fixedTotalDuration /= speedUpPct()
        val progress = Time.time % fixedTotalDuration / fixedTotalDuration//percent
        val index = (progress * it).toInt()
        return@IFrameIndexer index.coerceIn(0, it - 1)
    } else IFrameIndexer {
        val fixedTotalDuration = duration / speedUpPct()
        val progress = Time.time % fixedTotalDuration / fixedTotalDuration//percent
        val index = (progress * it).toInt()
        return@IFrameIndexer index.coerceIn(0, it - 1)
    }
@JvmOverloads
fun Animation.ixReciprocate(tileEntity: Building? = null) =
    if (tileEntity != null) IFrameIndexer {
        val endI = it - 1
        val fixedTotalDuration = duration * 2f / tileEntity.timeScale
        val progress = Time.time % fixedTotalDuration / fixedTotalDuration//percent
        var index = (progress * it * 2).toInt()
        if (index > endI) {
            index -= (index - endI) * 2
        }
        return@IFrameIndexer index.coerceIn(0, it - 1)
    }
    else IFrameIndexer {
        val endI = it - 1
        val fixedTotalDuration = duration * 2f
        val progress = Time.time % fixedTotalDuration / fixedTotalDuration//percent
        var index = (progress * it * 2).toInt()
        if (index > endI) {
            index -= (index - endI) * 2
        }
        return@IFrameIndexer index.coerceIn(0, endI)
    }

fun Animation.reciprocate() =
    this.indexer(ixReciprocate())

fun Animation.auto() =
    this.indexer(ixAuto())

fun AutoAnimation(duration: Float, vararg allFrames: TextureRegion) =
    Animation(duration, *allFrames).auto()

fun AnimationObj.byTimeScale(tileEntity: Building) =
    this.tmod { it / tileEntity.timeScale }
