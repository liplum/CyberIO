package net.liplum.utils

import arc.graphics.g2d.TextureRegion
import arc.scene.style.TextureRegionDrawable
import mindustry.ctype.UnlockableContent
import mindustry.world.Block
import net.liplum.ResourceLoader
import net.liplum.common.util.sheetOneDirection
import plumy.core.ClientOnly
import net.liplum.render.GlobalAnimation
import net.liplum.render.GlobalAnimation.Companion.register

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
            a.allFrames = name.sheetOneDirection(number = frameCount)
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
            a.allFrames = name.sheetOneDirection(number = frameCount)
        }
    }
    return this
}