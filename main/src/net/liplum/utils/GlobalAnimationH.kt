package net.liplum.utils

import arc.graphics.g2d.TextureRegion
import arc.scene.style.TextureRegionDrawable
import mindustry.ctype.UnlockableContent
import mindustry.world.Block
import net.liplum.ResourceLoader
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.animations.ganim.GlobalAnimation
import net.liplum.mdt.animations.ganim.GlobalAnimation.Companion.register
import net.liplum.mdt.utils.anim
import net.liplum.mdt.utils.sheet

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
            a.allFrames = this.anim(number = frameCount)
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
            a.allFrames = this.anim(number = frameCount)
        }
    }
    return this
}

fun <T : TextureRegionDrawable> T.globalAnim(
    name: String,
    duration: Float, frameCount: Int,
    config: GlobalAnimation.() -> Unit = {},
): T {
    ClientOnly {
        val a = GlobalAnimation(duration) {
            this.region = it
        }.apply {
            config()
            register()
        }
        ResourceLoader += {
            a.allFrames = name.sheet(number = frameCount)
        }
    }
    return this
}

fun <T : TextureRegion> T.globalAnim(
    name: String,
    duration: Float, frameCount: Int,
    config: GlobalAnimation.() -> Unit = {},
): T {
    ClientOnly {
        val a = GlobalAnimation(duration) {
            this.set(it)
        }.apply {
            config()
            register()
        }
        ResourceLoader += {
            a.allFrames = name.sheet(number = frameCount)
        }
    }
    return this
}