package net.liplum.animations

import arc.Events
import arc.func.Cons
import arc.graphics.g2d.TextureRegion
import arc.math.Mathf
import arc.util.Time
import mindustry.game.EventType

class GlobalAnimation(
    val duration: Float,
    val frames: Array<TextureRegion>,
    val setTR: Cons<TextureRegion>
) : IGlobalAnimation {
    override var needUpdate: Boolean = true
    var lastTR: TextureRegion? = null
    private var registered: Boolean = false
    private fun getCurTR(): TextureRegion {
        val progress = Time.time % duration / duration //percent
        var index: Int = (progress * frames.size).toInt()
        index = Mathf.clamp(index, 0, frames.size)
        return frames[index]
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

    fun register():GlobalAnimation {
        if (!registered) {
            registered = true
            Events.run(EventType.Trigger.update, this::update)
        }
        return this
    }
}