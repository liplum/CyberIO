package net.liplum.mdt.utils

import arc.Core
import arc.graphics.g2d.TextureRegion
import mindustry.ctype.MappableContent
import net.liplum.common.utils.sheetOneDirection
import net.liplum.mdt.animations.anims.Animation
import net.liplum.mdt.animations.anims.AutoAnimation

@JvmOverloads
fun MappableContent.autoAnim(
    subName: String? = null,
    frame: Int,
    totalDuration: Float,
): Animation = AutoAnimation(totalDuration, *this.anim(subName, true, frame))

fun MappableContent.autoAnimInMod(
    name: String,
    frame: Int,
    totalDuration: Float,
): Animation {
    val identity = "${this.minfo.mod.name}-$name-anim"
    val tr: TextureRegion = Core.atlas.find(identity)
    return AutoAnimation(totalDuration, *tr.sheetOneDirection(number = frame))
}
