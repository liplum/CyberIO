package net.liplum.mdt.utils

import arc.util.Time
import net.liplum.lib.utils.format

val Float.seconds: Int
    get() = (this / Time.toSeconds).toInt()

fun Float.toSeconds(digits: Int): String = (this / Time.toSeconds).format(digits)
val Float.draw: Float
    get() = this - 90