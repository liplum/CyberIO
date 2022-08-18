package net.liplum.mdt.utils

import arc.util.Time
import net.liplum.common.util.format

val Float.seconds: Int
    get() = (this / Time.toSeconds).toInt()

val Float.draw: Float
    get() = this - 90