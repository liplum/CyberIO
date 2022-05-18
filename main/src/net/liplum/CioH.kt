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