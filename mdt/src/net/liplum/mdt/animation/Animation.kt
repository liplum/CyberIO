package net.liplum.mdt.animation

import net.liplum.common.ITimer
import net.liplum.common.util.progress
import net.liplum.mdt.animation.AnimationBehaviors.linerLoopByTime
import plumy.core.arc.Tick
import plumy.core.assets.EmptyTRs
import plumy.core.assets.TR
import plumy.core.assets.TRs

typealias AnimationBehavior = (all: TRs, time: Tick, duration: Tick) -> TR

class AnimationMeta(
    val allFrames: TRs,
    val duration: Tick,
    val behavior: AnimationBehavior = linerLoopByTime,
) {
    fun instantiate() = Animation(
        allFrames = allFrames,
        duration = duration,
        behavior = behavior
    )

    companion object {
        val Empty = AnimationMeta(EmptyTRs, 0f)
    }
}

class Animation(
    val allFrames: TRs,
    val duration: Tick,
    val behavior: AnimationBehavior = linerLoopByTime,
) : IFramed, ITimer {
    var curTime = 0f
    override fun spend(time: Float) {
        this.curTime += time
    }

    override val curFrame: TR
        get() = behavior(allFrames, curTime, duration)

    companion object {
        val Empty = Animation(EmptyTRs, 0f)
    }
}

object AnimationBehaviors {
    val linerLoopByTime: AnimationBehavior = { all, time, duration ->
        all.progress(time % duration / duration)
    }
}
private typealias SAB = SharedAnimationBehaviors

val behaviorMapping = mutableMapOf(
    SAB.linerLoopByTime to linerLoopByTime
)

fun SharedAnimation.instantiate(
    behavior: AnimationBehavior = behaviorMapping[this.behavior] ?: linerLoopByTime,
) = Animation(
    allFrames = allFrames,
    duration = duration,
    behavior = behavior,
)