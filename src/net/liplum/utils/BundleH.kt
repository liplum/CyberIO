package net.liplum.utils

import arc.Core
import arc.files.Fi
import arc.util.I18NBundle
import arc.util.io.PropertiesUtils
import net.liplum.R
import net.liplum.lib.Res
import java.io.Reader
import java.util.*
import kotlin.math.absoluteValue

fun String.bundle(vararg args: Any): String = Core.bundle.format(this, *args)
fun String.bundle(bundle: I18NBundle, vararg args: Any): String = bundle.format(this, *args)
val String.bundle: String
    get() = Core.bundle.format(this)

fun String.bundle(bundle: I18NBundle): String = bundle[this]
fun Boolean.yesNo(): String =
    if (this)
        R.Ctrl.Yes.bundle
    else
        R.Ctrl.No.bundle
@JvmOverloads
fun Int.time(coerceMinute: Boolean = false): String {
    val min = this / 60
    val sec = this % 60
    return if (min > 0 || coerceMinute)
        "$min ${R.Bundle.CostMinute.bundle} $sec ${R.Bundle.CostSecond.bundle}"
    else
        "$sec ${R.Bundle.CostSecond.bundle}"
}
@JvmOverloads
fun Float.percent(digit: Int = 2) =
    if (this >= 0f) {
        R.Bundle.PercentPlus.bundle((this * 100f).absoluteValue.format(digit))
    } else {
        R.Bundle.PercentMinus.bundle((this * 100f).absoluteValue.format(digit))
    }

fun Int.percent() =
    if (this >= 0) {
        R.Bundle.PercentPlus.bundle(this.absoluteValue)
    } else {
        R.Bundle.PercentMinus.bundle(this.absoluteValue)
    }
@JvmOverloads
fun Float.value(digit: Int = 2) =
    if (this >= 0f) {
        R.Bundle.ValuePlus.bundle(this.absoluteValue.format(digit))
    } else {
        R.Bundle.ValueMinus.bundle(this.absoluteValue.format(digit))
    }

fun Int.value() =
    if (this >= 0) {
        R.Bundle.ValuePlus.bundle(this.absoluteValue)
    } else {
        R.Bundle.ValueMinus.bundle(this.absoluteValue)
    }

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
fun String.handleBundleRefer(maxDepth: Int = 16): String = this.handleBundleRefer(Core.bundle, maxDepth)
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
    bundle.setF("locale", locale)
    bundle.setF("formatter", formatter)
    bundle.setF("parent", parent)
    return bundle
}
@JvmInline
value class ReferBundleWrapper(
    val bundle: I18NBundle,
) {
    operator fun get(key: String): String =
        bundle[key].handleBundleRefer(bundle)

    operator fun get(key: String, default: String): String =
        bundle[key, default].handleBundleRefer(bundle)

    fun has(key: String) =
        bundle.has(key)

    operator fun contains(key: String) =
        bundle.has(key)

    fun format(key: String, vararg args: Any?): String =
        bundle.format(key, *args)

    fun loadMoreFrom(folder: String, defaultLocale: String = "en") {
        bundle.loadMoreFrom(folder, defaultLocale)
    }

    companion object {
        fun create(): ReferBundleWrapper =
            ReferBundleWrapper(createModBundle())
    }
}