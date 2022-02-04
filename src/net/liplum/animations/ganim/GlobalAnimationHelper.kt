package net.liplum.animations.ganim

import net.liplum.GameHelper.Companion.ClientOnly
import net.liplum.blocks.AnimedBlock
import net.liplum.utils.animA

fun <T : AnimedBlock> T.animation(duration: Float, frameCount: Int): T {
    ClientOnly {
        val a = GlobalAnimation(duration) {
            this.region.set(it)
        }.register()
        this.addLoadListener {
            a.frames = this.animA(null, frameCount)
        }
    }
    return this
}