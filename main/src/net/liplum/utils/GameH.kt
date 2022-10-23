@file:JvmName("GameH")

package net.liplum.utils

import arc.util.Log
import mindustry.Vars
import mindustry.gen.Teamc
import net.liplum.common.Condition
import java.lang.annotation.Inherited
import kotlin.annotation.AnnotationTarget.*

/**
 * It indicates this should be called or accessed only on Logical Server.
 * You should wrap this with [ServerOnly].
 */
@Retention(AnnotationRetention.SOURCE)
@Inherited
@MustBeDocumented
annotation class ServerOnly
/**
 * It indicates this will send data packet to synchronize no matter which Server/Client.
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
annotation class SendDataPack(
    val callChain: Array<String> = [],
)
/**
 * It indicates this will be called by a function which handles data packet.
 */
@Retention(AnnotationRetention.SOURCE)
@Inherited
@MustBeDocumented
annotation class CalledBySync
/**
 * It indicates something in the vanilla will be overwritten
 */
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class OverwriteVanilla(val value: String = "")

val IsClient = Condition { !Vars.headless }
val IsServer = Condition {
    val net = Vars.net
    net.server() || !net.active()
}
val IsLocal = Condition {
    val net = Vars.net
    !net.server() && !net.active()
}
val IsSteam = Condition { Vars.steam }
/**
 * Runs codes only on Logical Server
 */
inline fun ServerOnly(func: () -> Unit): Boolean {
    val net = Vars.net
    if (net.server() || !net.active()) {
        func()
        return true
    }
    return false
}

inline fun NetClientOnly(func: () -> Unit): Boolean {
    if (Vars.net.client()) {
        func()
        return true
    }
    return false
}
/**
 * Runs codes only on Logical Server
 */
inline fun OnlyLocal(func: () -> Unit): Boolean {
    val net = Vars.net
    if (!net.server() && !net.active()) {
        func()
    }
    return false
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
 * If an exception is thrown, it doesn't crash the game but outputs log.
 */
inline fun safeCall(msg: String? = null, func: () -> Unit) {
    try {
        func()
    } catch (e: Throwable) {
        if (msg != null) Log.err(msg)
        Log.err(e)
    }
}
/**
 * If an exception is thrown, it doesn't crash the game but outputs log.
 */
inline fun safeCall(func: () -> Unit) {
    try {
        func()
    } catch (e: Throwable) {
        Log.err(e)
    }
}
/**
 * If an exception is thrown, it doesn't crash the game without any log.
 */
inline fun safeCallSilent(func: () -> Unit) {
    try {
        func()
    } catch (_: Throwable) {
    }
}

inline fun Teamc.WhenTheSameTeam(func: () -> Unit): Boolean {
    if (team() == Vars.player.team()) {
        func()
        return true
    }
    return false
}