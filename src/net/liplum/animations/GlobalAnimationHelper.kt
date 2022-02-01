package net.liplum.animations

import net.liplum.blocks.AnimedBlock
import net.liplum.utils.AtlasUtil

fun <T : AnimedBlock> T.animation(duration: Float, frameCount: Int): T {
    val a = GlobalAnimation(duration) {
        this.region.set(it)
    }.register()
    this.addLoadListener {
        a.frames = AtlasUtil.animation(this, null, frameCount)
    }
    return this
}