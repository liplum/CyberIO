package net.liplum.utils

import arc.Core
import net.liplum.R

fun String.bundle(vararg args: Any): String {
    return Core.bundle.format(this, *args)
}

val String.bundle: String
    get() = Core.bundle.format(this)

fun Boolean.yesNo(): String = Core.bundle.format(
    if (this)
        R.Ctrl.Yes
    else
        R.Ctrl.No,
    this
)
