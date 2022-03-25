package net.liplum.utils

import arc.Core
import arc.graphics.g2d.TextureRegion
import mindustry.ctype.MappableContent
import net.liplum.Meta
import net.liplum.animations.anims.Animation
import net.liplum.animations.anims.AutoAnimation

@JvmOverloads
fun MappableContent.autoAnim(
    subName: String? = null,
    frame: Int,
    totalDuration: Float
): Animation {
    return AutoAnimation(totalDuration, *this.anim(subName, true, frame))
}

fun String.autoAnimInCio(
    frame: Int,
    totalDuration: Float
): Animation {
    val identity = "${Meta.ModID}-$this-anim"
    val tr: TextureRegion = Core.atlas.find(identity)
    return AutoAnimation(totalDuration, *tr.slice(frame))
}
