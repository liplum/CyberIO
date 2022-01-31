package net.liplum.animations

import mindustry.world.Block
import net.liplum.utils.AtlasUtil

fun <T:Block> T.animation(duration: Float, frameCount: Int):T {
    GlobalAnimation(duration, AtlasUtil.animation(this, null, frameCount)) {
        this.region.set(it)
    }.register()
    return this
}