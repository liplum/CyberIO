package net.liplum.mdt.animation

import mindustry.Vars
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
    /**
     * Instantiate an [Animation] with the metadata.
     * When this runs on [Vars.headless] side, it will return [Animation.Empty]
     */
    fun instantiate() = if (Vars.headless) Animation.Empty
    else Animation(
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
/**
 * Instantiate an [Animation] with the [SharedAnimation] as a metadata.
 * When this runs on [Vars.headless] side, it will return [Animation.Empty]
 */
fun SharedAnimation.instantiate(
    behavior: AnimationBehavior = behaviorMapping[this.behavior] ?: linerLoopByTime,
) = if (Vars.headless) Animation.Empty
else Animation(
    allFrames = allFrames,
    duration = duration,
    behavior = behavior,
)