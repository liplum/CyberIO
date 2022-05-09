package net.liplum.lib.animations.ganim

import arc.Events
import arc.func.Cons
import arc.graphics.g2d.TextureRegion
import arc.math.Mathf
import arc.util.Time
import mindustry.game.EventType
import net.liplum.ClientOnly
import net.liplum.lib.delegates.Delegate

typealias GlobalAnimationIndexer = GlobalAnimation.() -> TextureRegion

open class GlobalAnimation(
    val duration: Float,
    val setTR: Cons<TextureRegion>,
) : IGlobalAnimation {
    lateinit var frames: Array<TextureRegion>
    var frameIndexer: GlobalAnimationIndexer = loopIndexer
    override val canUpdate: Boolean
        get() = CanPlay && ResourceLoaded
    var lastTR: TextureRegion? = null
    protected fun getCurTR(): TextureRegion {
        return frameIndexer()
    }
    @ClientOnly
    override fun update() {
        if (canUpdate) {
            val curTR = getCurTR()
            if (curTR != lastTR) {
                lastTR = curTR
                this.setTR.get(curTR)
            }
        }
    }

    companion object {
        val loopIndexer: GlobalAnimationIndexer = {
            val frames = frames
            val progress = Time.globalTime % duration / duration //percent
            var index: Int = (progress * frames.size).toInt()
            index = Mathf.clamp(index, 0, frames.size)
            frames[index]
        }
        // TODO: that's too random.
        val randomSelectIndexr: GlobalAnimationIndexer = {
            frames.random()
        }
        var CanPlay = false
        var ResourceLoaded = false
        val updateTasks = HashSet<IGlobalAnimation>()
        val loadingTask: Delegate = Delegate()
        @ClientOnly
        @JvmStatic
        fun registerAll() {
            Events.run(EventType.Trigger.update) {
                if (CanPlay) {
                    for (task in updateTasks)
                        task.update()
                }
            }
        }

        fun GlobalAnimation.useRandom(): GlobalAnimation {
            this.frameIndexer = randomSelectIndexr
            return this
        }

        fun GlobalAnimation.register(): GlobalAnimation {
            updateTasks.add(this)
            return this
        }
        @ClientOnly
        fun globalPlay() {
            if (CanPlay && ResourceLoaded) {
                for (task in updateTasks)
                    task.update()
            }
        }
        @JvmStatic
        @ClientOnly
        fun loadAllResources() {
            loadingTask()
            loadingTask.clear()
            ResourceLoaded = true
        }
    }
}