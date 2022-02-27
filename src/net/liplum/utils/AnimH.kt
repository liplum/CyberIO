package net.liplum.utils

import mindustry.ctype.MappableContent
import net.liplum.animations.anims.blocks.AutoAnimationT

fun MappableContent.autoAnim(subName: String?, frame: Int, totalDuration: Float): AutoAnimationT =
    AnimU.auto(this, subName, frame, totalDuration)

fun String.autoCioAnim(frame: Int, totalDuration: Float): AutoAnimationT =
    AnimU.autoCio(this, frame, totalDuration)