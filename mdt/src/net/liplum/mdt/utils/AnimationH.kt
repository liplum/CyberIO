package net.liplum.mdt.utils

import mindustry.Vars
import mindustry.ctype.MappableContent
import net.liplum.common.util.sheetOneDirection
import net.liplum.mdt.animation.Animation
import net.liplum.mdt.animation.AnimationMeta
import net.liplum.mdt.animation.SharedAnimation

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

fun AnimationMeta.instantiateSideOnly() =
    if (Vars.headless) Animation.Empty else instantiate()