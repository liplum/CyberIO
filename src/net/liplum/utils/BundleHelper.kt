package net.liplum.utils

import arc.Core

fun String.bundle(vararg args: Any): String {
    return Core.bundle.format(this, *args)
}