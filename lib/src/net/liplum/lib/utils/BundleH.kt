@file:JvmName("BundleH")

/**
 * In this BundleH.kt, you can only import any class from [arc.*]
 */
package net.liplum.lib.utils

import arc.Core
import arc.files.Fi
import arc.struct.ObjectMap
import arc.util.I18NBundle
import arc.util.io.PropertiesUtils
import net.liplum.lib.Res
import net.liplum.lib.UseReflection
import java.io.Reader
import java.util.*

typealias BundleKey = String

fun String.bundle(vararg args: Any): String = Core.bundle.format(this, *args)
fun String.bundle(bundle: I18NBundle, vararg args: Any): String = bundle.format(this, *args)
private val bundle2Fields: MutableMap<I18NBundle, BundleFields> = HashMap()

class BundleFields(
    val properties: ObjectMap<String, String>,
    val formatter: Any,
) {
    @UseReflection
    val formatterFunc = formatter.javaClass.getMethodBy(
        "format",
        String::class.java, Array<Any>::class.java
    )

    operator fun get(key: String): String =
        properties[key]

    operator fun set(key: String, value: String) {
        properties[key] = value
    }
    /**
     * Find its value by key then format value
     */
    fun format(key: String, vararg args: Any) {
        formatterFunc(formatter, properties[key], args)
    }
    /**
     * Directly format this value
     */
    @UseReflection
    fun formatValue(value: String, vararg args: Any): String {
        return formatterFunc(formatter, value, args) as String
    }
}

private fun I18NBundle.getBundleFields(): BundleFields {
    return bundle2Fields.getOrPut(this) {
        BundleFields(
            this.getF("properties"),
            this.getF("formatter"),
        )
    }
}
@UseReflection
operator fun I18NBundle.set(key: String, value: String) {
    val fields = this.getBundleFields()
    fields.properties[key] = value
}
/**
 * Directly format this value
 */
@UseReflection
fun I18NBundle.formatDirectly(
    value: String,
    vararg args: Any,
): String {
    val fields = this.getBundleFields()
    return fields.formatValue(value, *args)
}

val String.bundle: String
    get() = Core.bundle.format(this)

fun String.bundle(bundle: I18NBundle): String = bundle[this]
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
/**
 * Link the parent bundle which supports a child bundle to get default text from it.
 */
@UseReflection
fun I18NBundle.linkParent(folder: String, parent: String = "en"): I18NBundle {
    val parentBundle = createModBundle()
    parentBundle.loadMoreFrom(folder, parent)
    this.setF("parent", parentBundle)
    return parentBundle
}
@UseReflection
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

interface IBundle {
    operator fun get(key: String): String
    operator fun set(key: String, value: String)
    fun getOrDefault(key: String, default: String): String
    operator fun contains(key: String): Boolean
    fun format(key: String, vararg args: Any): String
}
/**
 * If it has new bundle pair, use new pair
 */
class OverwriteBundle(
    val bundle: I18NBundle,
) : IBundle {
    /**
     * Key to value
     */
    var overwrites: MutableMap<String, String> = HashMap()
    /**
     * Key to value
     */
    fun overwrite(map: Map<String, String>): OverwriteBundle {
        overwrites.putAll(map)
        return this
    }
    /**
     * Key to value
     */
    fun overwrite(key: String, value: String): OverwriteBundle {
        overwrites[key] = value
        return this
    }

    override fun get(key: String): String =
        overwrites[key] ?: bundle[key]

    override fun set(key: String, value: String) {
        bundle[key] = value
    }

    override fun format(key: String, vararg args: Any): String =
        overwrites[key]?.format(*args) ?: bundle.format(key, *args)

    override fun getOrDefault(key: String, default: String): String =
        overwrites[key] ?: bundle[key, default]

    override fun contains(key: String) =
        key in overwrites || bundle.has(key)
}
/**
 * Original key to new Key.
 */
class MapKeyBundle(
    val bundle: I18NBundle,
) : IBundle {
    /**
     * Key to new key
     */
    var overwritesKey2Key: MutableMap<String, String> = HashMap()
    /**
     * Key to new key
     */
    fun overwrite(map: Map<String, String>): MapKeyBundle {
        overwritesKey2Key.putAll(map)
        return this
    }
    /**
     * Key to new key
     */
    fun overwrite(key: String, value: String): MapKeyBundle {
        overwritesKey2Key[key] = value
        return this
    }

    override fun get(key: String): String =
        overwritesKey2Key[key].let {
            if (it != null)
                bundle[it]
            else
                bundle[key]
        }

    override fun set(key: String, value: String) {
        bundle[key] = value
    }
    /**
     * If [key] in [overwritesKey2Key], format new key's value by [args]
     * If not, format [key] by [args]
     */
    override fun format(key: String, vararg args: Any): String =
        overwritesKey2Key[key].let {
            if (it != null)
                bundle.formatDirectly(it, *args)
            else
                bundle.format(key, *args)
        }

    override fun getOrDefault(key: String, default: String): String =
        overwritesKey2Key[key].let {
            if (it != null)
                bundle[it, default]
            else
                bundle[key, default]
        }

    override fun contains(key: String) =
        key in overwritesKey2Key || bundle.has(key)
}
@JvmInline
value class ReferBundleWrapper(
    val bundle: I18NBundle,
) : IBundle {
    override operator fun get(key: String): String =
        bundle[key].handleBundleRefer(bundle)

    override fun getOrDefault(key: String, default: String) =
        bundle[key, default].handleBundleRefer(bundle)

    operator fun get(key: String, default: String): String =
        bundle[key, default].handleBundleRefer(bundle)

    fun has(key: String) =
        bundle.has(key)

    override fun set(key: String, value: String) {
        bundle[key] = value
    }

    override operator fun contains(key: String) =
        bundle.has(key)

    override fun format(key: String, vararg args: Any): String =
        bundle.format(key, *args)

    fun loadMoreFrom(folder: String, defaultLocale: String = "en") {
        bundle.loadMoreFrom(folder, defaultLocale)
    }

    fun linkParent(folder: String, parent: String = "en"): I18NBundle {
        return bundle.loadMoreFrom(folder, parent)
    }

    fun handleRefer(text: String): String =
        if (has(text))
            bundle[text]
        else
            text

    companion object {
        fun create(): ReferBundleWrapper =
            ReferBundleWrapper(createModBundle())
    }
}

interface IBundlable {
    val bundlePrefix: String
    val parentBundle: IBundlable?
        get() = null

    fun bundle(key: String, vararg args: Any) =
        if (args.isEmpty()) {
            val parent = parentBundle
            if (parent != null) "${parent.bundlePrefix}.${bundlePrefix}.$key".bundle
            else "${bundlePrefix}.$key".bundle
        } else {
            val parent = parentBundle
            if (parent != null) "${parent.bundlePrefix}.${bundlePrefix}.$key".bundle(*args)
            else "${bundlePrefix}.$key".bundle(*args)
        }
}