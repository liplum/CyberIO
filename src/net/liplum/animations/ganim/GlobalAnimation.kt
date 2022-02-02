package net.liplum.animations.ganim

import arc.Events
import arc.func.Cons
import arc.graphics.g2d.TextureRegion
import arc.math.Mathf
import arc.util.Time
import mindustry.game.EventType
import net.liplum.GameHelper.Companion.ClientOnly

class GlobalAnimation(
    val duration: Float,
    val setTR: Cons<TextureRegion>
) : IGlobalAnimation {
    var frames: Array<TextureRegion>? = null
    override var needUpdate: Boolean = true
        get() {
            return field && frames != null
        }
    var lastTR: TextureRegion? = null
    private var registered: Boolean = false
    private fun getCurTR(): TextureRegion {
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
                Events.run(EventType.Trigger.update, this::update)
            }
        }
        return this
    }
}