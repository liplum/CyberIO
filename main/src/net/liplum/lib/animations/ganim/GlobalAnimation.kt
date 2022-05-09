package net.liplum.lib.animations.ganim

import arc.Events
import arc.func.Cons
import arc.graphics.g2d.TextureRegion
import arc.math.Mathf
import arc.util.Time
import mindustry.game.EventType
import net.liplum.CanRefresh
import net.liplum.CioMod
import net.liplum.ClientOnly
import net.liplum.lib.delegates.Delegate

open class GlobalAnimation(
    val duration: Float,
    val setTR: Cons<TextureRegion>
) : IGlobalAnimation {
    var frames: Array<TextureRegion>? = null
    override val needUpdate: Boolean
        get() {
            return CioMod.CanGlobalAnimationPlay && CanRefresh() && frames != null
        }
    var lastTR: TextureRegion? = null
    protected var registered: Boolean = false
    protected fun getCurTR(): TextureRegion {
        val progress = Time.time % duration / duration //percent
        var index: Int = (progress * frames!!.size).toInt()
        index = Mathf.clamp(index, 0, frames!!.size)
        return frames!![index]
    }

    override fun update() {
        if (needUpdate) {
            val curTR = getCurTR()
            if (curTR != lastTR) {
                lastTR = curTR
                this.setTR.get(curTR)
            }
        }
    }

    fun register(): GlobalAnimation {
        ClientOnly {
            if (!registered) {
                registered = true
                Events.run(EventType.Trigger.draw, this::update)
            }
        }
        return this
    }

    companion object {
        val loadingTask: Delegate = Delegate()
        @JvmStatic
        fun loadAllResources() {
            loadingTask()
            loadingTask.clear()
        }
    }
}