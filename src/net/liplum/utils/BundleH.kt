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
/**
 * Handle with the reference in bundle.
 *
 * To prevent stack overflow, this uses loop instead of recursion
 * @param maxDepth to prevent infinite loop, please set an appropriate value.
 */
fun String.handleBundleRefer(maxDepth: Int = 16): String {
    var curStr = this
    for (i in 0 until maxDepth) {
        if (curStr.startsWith('@'))
            curStr = curStr.substring(1).bundle
        else
            break
    }
    return curStr
}