@file:JvmName("GameH")

package net.liplum

import arc.util.Time
import mindustry.Vars
import net.liplum.lib.Condition
import net.liplum.utils.format
import java.lang.annotation.Inherited
import kotlin.annotation.AnnotationTarget.*

/**
 * It indicates this should be called or accessed only on Physical Client
 */
@Retention(AnnotationRetention.SOURCE)
@Inherited
@MustBeDocumented
annotation class ClientOnly
/**
 * It indicates this should be called or accessed only on Logical Server
 */
@Retention(AnnotationRetention.SOURCE)
@Inherited
@MustBeDocumented
annotation class ServerOnly
/**
 * It indicates this should be called or accessed only on Physical Server
 */
@Retention(AnnotationRetention.SOURCE)
@Inherited
@MustBeDocumented
annotation class HeadlessOnly
/**
 * It indicates this will send data packet to synchronize no matter which Server/Client
 */
@Retention(AnnotationRetention.SOURCE)
@Target(
    FUNCTION,
    PROPERTY_SETTER,
    CONSTRUCTOR,
    PROPERTY,
)
@Inherited
@MustBeDocumented
annotation class SendDataPack
/**
 * It indicates this will be called by a function which handles data packet
 */
@Retention(AnnotationRetention.SOURCE)
@Inherited
@MustBeDocumented
annotation class CalledBySync
/**
 * It indicates this function use random number which may not be synchronized on Physical Server between Physical Client
 * so that you have to send data packet manually to share data.
 */
@Retention(AnnotationRetention.SOURCE)
@Inherited
@MustBeDocumented
annotation class UseRandom
@Retention(AnnotationRetention.SOURCE)
/**
 * It indicates reflection is used there. Please pay attention to the API changes between versions.
 */
@Target(
    FUNCTION,
    PROPERTY_SETTER,
    PROPERTY_GETTER,
    CONSTRUCTOR,
    PROPERTY,
    EXPRESSION,
    LOCAL_VARIABLE,
)
@MustBeDocumented
annotation class UseReflection
/**
 * Runs codes only on Physical Client
 */
inline fun ClientOnly(func: () -> Unit): Boolean {
    if (!Vars.headless) {
        func()
        return true
    }
    return false
}

inline fun DesktopOnly(func: () -> Unit): Boolean {
    if (!Vars.mobile) {
        func()
        return true
    }
    return false
}

inline fun MobileOnly(func: () -> Unit): Boolean {
    if (Vars.mobile) {
        func()
        return true
    }
    return false
}

inline fun <reified T> T.DesktopOnlyOn(func: T.() -> Unit): T {
    if (!Vars.mobile) {
        func()
    }
    return this
}

inline fun <reified T> T.MobileOnlyOn(func: T.() -> Unit): T {
    if (Vars.mobile) {
        func()
    }
    return this
}

val OnlyClient = Condition {
    !Vars.headless
}
val OnlyServer = Condition {
    val net = Vars.net
    net.server() || !net.active()
}
val OnlyDebug = Condition {
    CioMod.DebugMode
}
val OnlySteam = Condition {
    Vars.steam
}
val NotSteam = !OnlySteam
inline fun <reified T> T.ClientOnlyOn(func: T.() -> Unit): T {
    if (!Vars.headless) {
        func()
    }
    return this
}
/**
 * Runs codes only on Logical Server
 */
inline fun ServerOnly(func: () -> Unit): Boolean {
    val net = Vars.net
    if (net.server() || !net.active()) {
        func()
    }
    return false
}

inline fun <reified T> T.ServerOnlyOn(func: T.() -> Unit): T {
    val net = Vars.net
    if (net.server() || !net.active()) {
        func()
    }
    return this
}

fun IsServer(): Boolean {
    val net = Vars.net
    return net.server() || !net.active()
}
/**
 * Runs codes only on Physical Server
 */
inline fun HeadlessOnly(func: () -> Unit): Boolean {
    if (Vars.headless) {
        func()
    }
    return false
}
/**
 * Runs codes only on Physical Server
 */
inline fun <reified T> T.HeadlessOnlyOn(func: T.() -> Unit): Boolean {
    if (Vars.headless) {
        func()
    }
    return false
}

inline fun WhenCanGlobalAnimationPlay(func: () -> Unit): Boolean {
    if (CioMod.CanGlobalAnimationPlay) {
        func()
        return true
    }
    return false
}

inline fun DebugOnly(func: () -> Unit): Boolean {
    if (CioMod.DebugMode) {
        func()
        return true
    }
    return false
}

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

inline fun SteamOnly(func: () -> Unit): Boolean {
    if (Vars.steam) {
        func()
        return true
    }
    return false
}

inline fun UnsteamOnly(func: () -> Unit): Boolean {
    if (!Vars.steam) {
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

inline infix fun Boolean.Else(func: () -> Unit) {
    if (!this) {
        func()
    }
}

fun CanRefresh() = Time.time % CioMod.UpdateFrequency < 1f
inline fun WhenRefresh(func: () -> Unit): Boolean {
    if (!Vars.state.isPaused && Time.time % CioMod.UpdateFrequency < 1f) {
        func()
        return true
    }
    return false
}

inline fun WhenNotPaused(func: () -> Unit) {
    if (!Vars.state.isPaused) {
        func()
    }
}

val Float.seconds: Int
    get() = (this / Time.toSeconds).toInt()

fun Float.toSeconds(digits: Int): String = (this / Time.toSeconds).format(digits)
val Float.draw: Float
    get() = this - 90
val String.Cio: String
    get() = R.Gen(this)
