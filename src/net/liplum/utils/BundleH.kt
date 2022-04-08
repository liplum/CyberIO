package net.liplum.utils

import arc.Core
import arc.files.Fi
import arc.util.I18NBundle
import arc.util.io.PropertiesUtils
import net.liplum.R
import java.io.Reader

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

fun I18NBundle.loadMore(file: Fi) {
    loadMore(file.reader())
}

fun I18NBundle.loadMore(reader: Reader) {
    PropertiesUtils.load(properties, reader)
}