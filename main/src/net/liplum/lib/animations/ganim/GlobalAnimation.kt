package net.liplum.lib.animations.ganim

import arc.Events
import arc.func.Cons
import arc.graphics.g2d.TextureRegion
import arc.math.Mathf
import arc.util.Time
import mindustry.game.EventType
import net.liplum.lib.delegates.Delegate

open class GlobalAnimation(
    val duration: Float,
    val setTR: Cons<TextureRegion>,
) : IGlobalAnimation {
    var frames: Array<TextureRegion>? = null
    override val canUpdate: Boolean
        get() = frames != null
    var lastTR: TextureRegion? = null
    protected fun getCurTR(): TextureRegion {
        val frames = frames!!
        val progress = Time.globalTime % duration / duration //percent
        var index: Int = (progress * frames.size).toInt()
        index = Mathf.clamp(index, 0, frames.size)
        return frames[index]
    }

    override fun update() {
        if (canUpdate) {
            val curTR = getCurTR()
            if (curTR != lastTR) {
                lastTR = curTR
                this.setTR.get(curTR)
            }
        }
    }

    fun register(): GlobalAnimation {
        updateTasks.add(this)
        return this
    }

    companion object {
        var CanPlay = false
        val updateTasks = HashSet<IGlobalAnimation>()
        val loadingTask: Delegate = Delegate()
        fun registerAll() {
            Events.run(EventType.Trigger.update) {
                if (CanPlay) {
                    for (task in updateTasks)
                        task.update()
                }
            }
        }
        @JvmStatic
        fun loadAllResources() {
            loadingTask()
            loadingTask.clear()
        }
    }
}