package net.liplum.animations.ganim

import net.liplum.ClientOnly
import net.liplum.blocks.AnimedBlock
import net.liplum.utils.anim

fun <T : AnimedBlock> T.animation(duration: Float, frameCount: Int): T {
    ClientOnly {
        val a = GlobalAnimation(duration) {
            this.region.set(it)
            this.fullIcon.set(it)
        }.register()
        this.addLoadListener {
            a.frames = this.anim(number = frameCount)
        }
    }
    return this
}