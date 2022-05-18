package net.liplum.mdt.animations.anims

import arc.graphics.Color
import arc.math.Mathf
import net.liplum.lib.Draw
import net.liplum.lib.ITimer
import net.liplum.lib.Reset
import net.liplum.lib.SetColor
import net.liplum.lib.TR

data class Frame(
    val image: TR,
    val duration: Float
)
/**
 * The anime will stop after a single playing ends(the same as reversed playing).
 */
class Anime(
    val frames: Array<Frame>
) : IAnimated, ITimer {
    init {
        assert(frames.isNotEmpty()) {
            "The frame is empty."
        }
    }

    operator fun get(index: Int): TR =
        frames[index].image

    var index = 0
        set(value) {
            field = value.coerceIn(0, frames.size - 1)
        }
    var curTime = 0f

    var isForward = { true }
    val curDuration: Float
        get() = frames[index].duration
    val curImage: TR
        get() = frames[index].image
    var onEnd = {}
    var isEnd: Boolean = false
    fun restart() {
        if (isForward()) {
            index = 0
            curTime = 0f
        } else {
            index = frames.size - 1
            curTime = curDuration
        }
        isEnd = false
    }

    override fun spend(time: Float) {
        if (isEnd) {
            onEnd()
            // If it still ends, just skip
            if(isEnd)
                return
        }
        if (isForward()) {
            curTime += time
            if (curTime > curDuration) {
                curTime %= curDuration
                index++
                curTime = curTime.coerceIn(0f, curDuration)
                if (index == frames.size - 1) {
                    isEnd = true
                    onEnd()
                }
            }
        } else {
            curTime -= time
            if (curTime < 0) {
                index--
                curTime += curDuration
                curTime = curTime.coerceIn(0f, curDuration)
                if (index == 0) {
                    isEnd = true
                    onEnd()
                }
            }
        }
    }

    override fun draw(x: Float, y: Float, rotation: Float) {
        curImage.Draw(x, y, rotation)
    }

    override fun draw(color: Color, x: Float, y: Float, rotation: Float) {
        SetColor(color)
        curImage.Draw(x, y, rotation)
        Reset()
    }

    override fun draw(howToRender: IHowToRender) {
        howToRender.render(curImage)
        Reset()
    }

    override fun draw(indexer: IFrameIndexer, howToRender: IHowToRender) {
        val curIndex = indexer.getCurIndex(frames.size)
        val curImage = frames[curIndex.coerceIn(0, frames.size - 1)].image
        howToRender.render(curImage)
    }
}
fun Anime.randomCurTime(): Anime {
    curTime = Mathf.random(curDuration)
    return this
}
fun Anime.setEnd(): Anime {
    isEnd = true
    index = frames.size -1
    return this
}
fun Anime.loop(): Anime {
    onEnd = {
        isEnd = false
        index = 0
    }
    return this
}

fun Anime.pingpong(): Anime {
    var forward = true
    isForward = { forward }
    onEnd = {
        forward = !forward
        isEnd = false
    }
    return this
}
/**
 * Generate frames of a linear animation.
 * @receiver an array of texture which is of non-zero size.
 * @param duration total duration of these frames
 * @return the corresponding frames
 */
fun Array<TR>.linearFrames(duration: Float): Array<Frame> =
    Array(size) {
        Frame(this[it], duration / size)
    }
/**
 * Generate frames of an interpolated animation.
 * @receiver an array of texture which is of non-zero size.
 * @param duration total duration of these frames
 * @param interpolation an increasing function and x∈[0,1], y∈[0,1]
 * @return the corresponding frames
 */
fun Array<TR>.genFramesBy(duration: Float, interpolation: (Float) -> Float): Array<Frame> {
    if (size == 1)
        return arrayOf(Frame(this[0], duration))
    var accumulation = 0f
    return Array(size) {
        val curDuration = interpolation((it + 1f) / size) * duration
        val increase = curDuration - accumulation
        accumulation = curDuration
        Frame(this[it], increase)
    }
}
