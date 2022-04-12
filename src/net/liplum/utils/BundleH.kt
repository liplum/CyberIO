package net.liplum.utils

import arc.Core
import arc.files.Fi
import arc.util.I18NBundle
import arc.util.io.PropertiesUtils
import net.liplum.R
import net.liplum.lib.Res
import java.io.Reader
import java.util.*

fun String.bundle(vararg args: Any): String {
    return Core.bundle.format(this, *args)
}

fun String.bundle(bundle: I18NBundle, vararg args: Any): String {
    return bundle.format(this, *args)
}

val String.bundle: String
    get() = Core.bundle.format(this)

fun String.bundle(bundle: I18NBundle): String {
    return bundle[this]
}

fun Boolean.yesNo(): String = Core.bundle.format(
    if (this)
        R.Ctrl.Yes
    else
        R.Ctrl.No,
    this
)

fun I18NBundle.loadMore(file: Fi) {
    file.reader().use { loadMore(it) }
}

fun I18NBundle.loadMore(reader: Reader) {
    reader.use { PropertiesUtils.load(properties, reader) }
}
/**
 * Handle with the reference in default bundle.
 *
 * To prevent stack overflow, this uses loop instead of recursion
 * @param maxDepth to prevent infinite loop, please set an appropriate value.
 */
fun String.handleBundleRefer(maxDepth: Int = 16): String {
    return this.handleBundleRefer(Core.bundle, maxDepth)
}
/**
 * Handle with the reference in specified bundle.
 *
 * To prevent stack overflow, this uses loop instead of recursion
 * @param maxDepth to prevent infinite loop, please set an appropriate value.
 */
fun String.handleBundleRefer(bundle: I18NBundle, maxDepth: Int = 16): String {
    var curStr = this
    for (i in 0 until maxDepth) {
        if (curStr.startsWith('@'))
            curStr = bundle[curStr.substring(1)]
        else
            break
    }
    return curStr
}
/**
 * Load i18n bundle from a folder with ```Core.settings.getString("locale")```.
 *
 * @param folder the folder name
 *
 * @param defaultLang if  not found, use this locale
 */
fun I18NBundle.loadMoreFrom(folder: String, defaultLocale: String = "en"): I18NBundle {
    val locale = Core.settings.getString("locale")
    Res("$folder/$locale.properties").tryLoad {
        loadMore(reader())
    }.whenNotFound {
        loadMore(Res("$folder/$defaultLocale.properties").reader())
    }
    return this
}

fun createModBundle(): I18NBundle {
    val bundle = I18NBundle.createEmptyBundle()
    val globalBundle = Core.bundle
    val locale = ReflectU.get<Locale>(globalBundle, "locale")
    val formatter = ReflectU.get<Any>(globalBundle, "formatter")
    val parent = ReflectU.get<Any>(globalBundle, "parent")
    bundle.set("locale", locale)
    bundle.set("formatter", formatter)
    bundle.set("parent", parent)
    return bundle
}