package net.liplum.utils

import mindustry.ctype.MappableContent
import mindustry.gen.Building
import net.liplum.animations.anims.blocks.AutoAnimationT

fun MappableContent.autoAnim(subName: String?, frame: Int, totalDuration: Float): AutoAnimationT<Building> =
    AnimU.auto(this, subName, frame, totalDuration)

fun String.autoCioAnim(frame: Int, totalDuration: Float): AutoAnimationT<Building> =
    AnimU.autoCio(this, frame, totalDuration)

fun <T> MappableContent.autoAnimT(
    subName: String?,
    frame: Int,
    totalDuration: Float
): AutoAnimationT<T> where T : Building =
    AnimU.autoT(this, subName, frame, totalDuration)

fun <T> String.autoCioAnimT(
    frame: Int,
    totalDuration: Float
): AutoAnimationT<T> where T : Building =
    AnimU.autoCioT(this, frame, totalDuration)