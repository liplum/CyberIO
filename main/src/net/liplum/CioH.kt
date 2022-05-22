@file:JvmName("CioH")

package net.liplum

import arc.Core
import arc.util.Time
import mindustry.Vars
import net.liplum.lib.Condition
import net.liplum.lib.TR
import net.liplum.lib.utils.bundle
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.utils.atlas
import java.lang.annotation.Inherited

val String.inCio: TR
    get() = Core.atlas.find("${Meta.ModID}-$this")
val String.Cio: String
    get() = R.Gen(this)

enum class ContentSpec(
    val id: String
) {
    Vanilla("vanilla"),
    Erekir("erekir");

    override fun toString() = id

    companion object {
        @JvmStatic
        fun String.resolveContentSpec() = when (this) {
            "erekir" -> Erekir
            else -> Vanilla
        }
    }
}

val String.spec: String
    get() = CioMod.ContentSpecific.suffixModVersion(this)

fun ContentSpec.suffixModVersion(version: String) =
    if (this != ContentSpec.Vanilla) "$version-$id"
    else version
@ClientOnly
val ContentSpec.i18nName: String
    get() = "${Meta.ModID}.spec.$id.name".bundle
@ClientOnly
val ContentSpec.i18nDesc: String
    get() = "${Meta.ModID}.spec.$id.desc".bundle
@ClientOnly
val ContentSpec.icon: TR
    get() = R.Gen("spec-$id").atlas()

inline fun VanillaSpec(func: () -> Unit) {
    if (CioMod.ContentSpecific == ContentSpec.Vanilla) {
        func()
    }
}

inline fun ErekirSpec(func: () -> Unit) {
    if (CioMod.ContentSpecific == ContentSpec.Erekir) {
        func()
    }
}
/**
 * It indicates this should be used when debugging.
 */
@Retention(AnnotationRetention.SOURCE)
@Inherited
@MustBeDocumented
annotation class DebugOnly

val OnlyDebug = Condition {
    CioMod.DebugMode
}

inline fun DebugOnly(func: () -> Unit): Boolean {
    if (CioMod.DebugMode) {
        func()
        return true
    }
    return false
}

inline fun <reified T> T.DebugOnlyOn(func: T.() -> Unit): T {
    if (CioMod.DebugMode) {
        func()
    }
    return this
}

fun CanRefresh() = Time.time % CioMod.UpdateFrequency < 1f
inline fun ExperimentalOnly(func: () -> Unit): Boolean {
    if (CioMod.ExperimentalMode) {
        func()
        return true
    }
    return false
}

inline fun UndebugOnly(func: () -> Unit): Boolean {
    if (!CioMod.DebugMode) {
        func()
        return true
    }
    return false
}

inline fun WhenRefresh(func: () -> Unit): Boolean {
    if (!Vars.state.isPaused && Time.time % CioMod.UpdateFrequency < 1f) {
        func()
        return true
    }
    return false
}