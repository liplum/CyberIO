package net.liplum.animations.ganim

import mindustry.ctype.UnlockableContent
import mindustry.world.Block
import net.liplum.ClientOnly
import net.liplum.utils.anim

fun <T : Block> T.globalAnim(duration: Float, frameCount: Int): T {
    ClientOnly {
        val a = GlobalAnimation(duration) {
            this.region.set(it)
            this.fullIcon.set(it)
            this.uiIcon.set(it)
        }.register()
        GlobalAnimation.loadingTask += {
            a.frames = this.anim(number = frameCount)
        }
    }
    return this
}

fun <T : UnlockableContent> T.globalAnim(duration: Float, frameCount: Int): T {
    ClientOnly {
        val a = GlobalAnimation(duration) {
            this.uiIcon.set(it)
            this.fullIcon.set(it)
        }.register()
        GlobalAnimation.loadingTask += {
            a.frames = this.anim(number = frameCount)
        }
    }
    return this
}