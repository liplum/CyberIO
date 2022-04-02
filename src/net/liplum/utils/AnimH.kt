package net.liplum.utils

import arc.Core
import arc.graphics.g2d.TextureRegion
import mindustry.ctype.MappableContent
import net.liplum.Meta
import net.liplum.lib.animations.anims.Animation
import net.liplum.lib.animations.anims.AutoAnimation

@JvmOverloads
fun MappableContent.autoAnim(
    subName: String? = null,
    frame: Int,
    totalDuration: Float
): Animation {
    return AutoAnimation(totalDuration, *this.anim(subName, true, frame))
}

fun MappableContent.autoAnimInMod(
    name: String,
    frame: Int,
    totalDuration: Float
): Animation {
    val identity = "${this.minfo.mod.name}-$name-anim"
    val tr: TextureRegion = Core.atlas.find(identity)
    return AutoAnimation(totalDuration, *tr.slice(frame))
}

fun String.autoAnimInCio(
    frame: Int,
    totalDuration: Float
): Animation {
    val identity = "${Meta.ModID}-$this-anim"
    val tr: TextureRegion = Core.atlas.find(identity)
    return AutoAnimation(totalDuration, *tr.slice(frame))
}
