package net.liplum.utils

import mindustry.ctype.MappableContent
import net.liplum.animations.anims.AutoAnimation

fun MappableContent.autoAnim(subName: String?, frame: Int, totalDuration: Float): AutoAnimation =
    AnimUtil.auto(this, subName, frame, totalDuration)

fun String.autoCioAnim(frame: Int, totalDuration: Float): AutoAnimation =
    AnimUtil.autoCio(this, frame, totalDuration)