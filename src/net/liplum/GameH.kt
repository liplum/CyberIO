@file:JvmName("GameH")

package net.liplum

import arc.util.Time
import mindustry.Vars
import net.liplum.utils.format

/**
 * It indicates this should be called or accessed only on Physical Client
 */
@Retention(AnnotationRetention.SOURCE)
annotation class ClientOnly
/**
 * It indicates this should be called or accessed only on Logical Server
 */
@Retention(AnnotationRetention.SOURCE)
annotation class ServerOnly
/**
 * It indicates this function use random number which may not be synchronized on Physical Server between Physical Client
 * so that you have to send data packet manually to share data.
 */
@Retention(AnnotationRetention.SOURCE)
annotation class UseRandom
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
    if (Time.time % CioMod.UpdateFrequency < 1f) {
        func()
        return true
    }
    return false
}

val Float.seconds: Int
    get() = (this / Time.toSeconds).toInt()

fun Float.toSeconds(digits: Int): String = (this / Time.toSeconds).format(digits)
val Float.draw: Float
    get() = this - 90