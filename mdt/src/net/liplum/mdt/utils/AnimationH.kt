package net.liplum.mdt.utils

import mindustry.ctype.MappableContent
import net.liplum.common.util.sheetOneDirection
import plumy.animation.AnimationMeta
import plumy.animation.SharedAnimation

fun MappableContent.sharedAnimation(
    subName: String? = null,
    frame: Int,
    totalDuration: Float,
) = SharedAnimation(this.anim(subName, true, frame), totalDuration)

fun MappableContent.animationMeta(
    subName: String? = null,
    frame: Int,
    totalDuration: Float,
) = AnimationMeta(this.anim(subName, true, frame), totalDuration)

fun MappableContent.sharedAnimationInMod(
    name: String,
    frame: Int,
    totalDuration: Float,
) = SharedAnimation(
    "${this.minfo.mod.name}-$name-anim".atlas().sheetOneDirection(number = frame),
    totalDuration
)