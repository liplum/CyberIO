@file:JvmName("CioH")

package net.liplum

import arc.Core
import arc.util.Time
import mindustry.Vars
import net.liplum.lib.Condition
import net.liplum.lib.TR
import java.lang.annotation.Inherited

val String.inCio: TR
    get() = Core.atlas.find("${Meta.ModID}-$this")
val String.Cio: String
    get() = R.Gen(this)

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
    Var.DebugMode
}

inline fun DebugOnly(func: () -> Unit): Boolean {
    if (Var.DebugMode) {
        func()
        return true
    }
    return false
}

inline fun <reified T> T.DebugOnlyOn(func: T.() -> Unit): T {
    if (Var.DebugMode) {
        func()
    }
    return this
}

fun CanRefresh() = Time.time % Var.UpdateFrequency < 1f
inline fun ExperimentalOnly(func: () -> Unit): Boolean {
    if (Var.ExperimentalMode) {
        func()
        return true
    }
    return false
}

inline fun UndebugOnly(func: () -> Unit): Boolean {
    if (!Var.DebugMode) {
        func()
        return true
    }
    return false
}

inline fun WhenRefresh(func: () -> Unit): Boolean {
    if (!Vars.state.isPaused && Time.time % Var.UpdateFrequency < 1f) {
        func()
        return true
    }
    return false
}