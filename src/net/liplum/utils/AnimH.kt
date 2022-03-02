package net.liplum.utils

import mindustry.ctype.MappableContent
import net.liplum.animations.anims.Animation

fun MappableContent.autoAnim(subName: String? = null, frame: Int, totalDuration: Float): Animation =
    AnimU.auto(this, subName, frame, totalDuration)

fun String.autoCioAnim(frame: Int, totalDuration: Float): Animation =
    AnimU.autoCio(this, frame, totalDuration)
