@file:JvmName("CioH")

package net.liplum

import arc.Core
import arc.util.Time
import mindustry.Vars
import net.liplum.common.Condition
import plumy.core.assets.TR
import java.lang.annotation.Inherited

val String.cioTR: TR
    get() = Core.atlas.find("${Meta.ModID}-$this")
val String.cio: String
    get() = R.Gen(this)

inline fun VanillaSpec(func: () -> Unit) {
    if (Var.ContentSpecific == ContentSpec.Vanilla) {
        func()
    }
}

inline fun ErekirSpec(func: () -> Unit) {
    if (Var.ContentSpecific == ContentSpec.Erekir) {
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
    Meta.EnableDebug
}

inline fun DebugOnly(func: () -> Unit): Boolean {
    if (Meta.EnableDebug) {
        func()
        return true
    }
    return false
}

enum class DebugLevel(
    val level: Int,
) {
    Any(0), Inspector(1), Important(2);
    /**
     * ## Use case:
     * ```
     * A = Current logging task level : 1
     * B = Global debug level : 2
     * `B.isIncluding(A) => 2 <= 1` is false
     * So run the logging task
     * ```
     */
    fun isIncluding(other: DebugLevel): Boolean =
        this.level <= other.level

    companion object {
        @JvmStatic
        fun valueOf(level: Int): DebugLevel =
            values()[level.coerceIn(0, values().size - 1)]

        val size: Int
            get() = values().size
    }
}

inline fun DebugOnly(level: DebugLevel, func: () -> Unit): Boolean {
    if (Meta.EnableDebug && Var.CurDebugLevel.isIncluding(level)) {
        func()
        return true
    }
    return false
}

fun CanRefresh() = Time.time % Var.AnimUpdateFrequency < 1f
inline fun ExperimentalOnly(func: () -> Unit): Boolean {
    if (Meta.EnableDebug) {
        func()
        return true
    }
    return false
}

inline fun UndebugOnly(func: () -> Unit): Boolean {
    if (!Meta.EnableDebug) {
        func()
        return true
    }
    return false
}

inline fun WhenRefresh(func: () -> Unit): Boolean {
    if (!Vars.state.isPaused && Time.time % Var.AnimUpdateFrequency < 1f) {
        func()
        return true
    }
    return false
}