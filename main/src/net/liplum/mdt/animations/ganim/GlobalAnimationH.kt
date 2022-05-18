package net.liplum.mdt.animations.ganim

import mindustry.ctype.UnlockableContent
import mindustry.world.Block
import net.liplum.mdt.ClientOnly
import net.liplum.ResourceLoader
import net.liplum.mdt.animations.ganim.GlobalAnimation.Companion.register
import net.liplum.mdt.utils.anim

fun <T : Block> T.globalAnim(
    duration: Float, frameCount: Int,
    config: GlobalAnimation.() -> Unit = {},
): T {
    ClientOnly {
        val a = GlobalAnimation(duration) {
            this.region.set(it)
            this.fullIcon.set(it)
            this.uiIcon.set(it)
        }.apply {
            config()
            register()
        }
        ResourceLoader += {
            a.frames = this.anim(number = frameCount)
        }
    }
    return this
}

fun <T : UnlockableContent> T.globalAnim(
    duration: Float, frameCount: Int,
    config: GlobalAnimation.() -> Unit = {},
): T {
    ClientOnly {
        val a = GlobalAnimation(duration) {
            this.uiIcon.set(it)
            this.fullIcon.set(it)
        }.apply {
            config()
            register()
        }
        ResourceLoader += {
            a.frames = this.anim(number = frameCount)
        }
    }
    return this
}