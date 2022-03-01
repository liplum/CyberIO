package net.liplum.utils

import mindustry.ctype.MappableContent
import net.liplum.animations.anims.blocks.AutoAnimation

fun MappableContent.autoAnim(subName: String?, frame: Int, totalDuration: Float): AutoAnimation =
    AnimU.auto(this, subName, frame, totalDuration)

fun String.autoCioAnim(frame: Int, totalDuration: Float): AutoAnimation =
    AnimU.autoCio(this, frame, totalDuration)
