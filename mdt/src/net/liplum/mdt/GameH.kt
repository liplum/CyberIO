@file:JvmName("GameH")

package net.liplum.mdt

import arc.Events
import arc.util.Log
import mindustry.Vars
import mindustry.game.EventType.Trigger
import mindustry.gen.Teamc
import net.liplum.lib.Condition
import java.lang.annotation.Inherited
import kotlin.annotation.AnnotationTarget.*

/**
 * It indicates this should be called or accessed only on Physical Client.
 * You should wrap this with [ClientOnly] or [ClientOnlyOn].
 * If a certain target isn't annotated this, it can be called on Physical Server(headless) safely.
 * ## Use case
 * 1. On properties or fields, you shouldn't access them, it may provide wrong data or even crash the game.
 * 2. On functions, you shouldn't call them, it can crash the game.
 * 3. On classes or objects, you must never load them into class loader, the static initialization can crash the game.
 */
@Retention(AnnotationRetention.SOURCE)
@Inherited
@MustBeDocumented
annotation class ClientOnly
/**
 * It indicates this should be called or accessed only on Logical Server.
 * You should wrap this with [ServerOnly] or [ServerOnlyOn].
 */
@Retention(AnnotationRetention.SOURCE)
@Inherited
@MustBeDocumented
annotation class ServerOnly
/**
 * It indicates this should be called or accessed only on Physical Server(headless).
 * You should wrap this with [HeadlessOnly] or [HeadlessOnlyOn].
 * If a certain target isn't annotated this, it can be called on Physical Client safely.
 * ## Use case
 * 1. On properties or fields, you shouldn't access them, it may provide wrong data or even crash the game.
 * 2. On functions, you shouldn't call them, it can crash the game.
 * 3. On classes or objects, you must never load them into class loader, the static initialization can crash the game.
 */
@Retention(AnnotationRetention.SOURCE)
@Inherited
@MustBeDocumented
annotation class HeadlessOnly
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
annotation class SendDataPack
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

inline fun <reified T> T.OnlyLocalOn(func: T.() -> Unit): T {
    val net = Vars.net
    if (!net.server() && !net.active()) {
        func()
    }
    return this
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

inline fun SteamOnly(func: () -> Unit): Boolean {
    if (Vars.steam) {
        func()
        return true
    }
    return false
}

inline fun UnsteamOnly(func: () -> Unit): Boolean {
    if (!(Vars.steam)) {
        func()
        return true
    }
    return false
}

inline infix fun Boolean.Else(func: () -> Unit) {
    if (!this) {
        func()
    }
}

inline fun WhenNotPaused(func: () -> Unit) {
    if (!Vars.state.isPaused) {
        func()
    }
}
/**
 * If an exception is thrown, it doesn't crash the game.
 */
inline fun safeCall(func: () -> Unit) {
    try {
        func()
    } catch (e: Throwable) {
        Log.err(e)
    }
}

inline fun RunOnUpdate(crossinline func: () -> Unit) {
    Events.run(Trigger.update) {
        func()
    }
}

inline fun RunOnDraw(crossinline func: () -> Unit) {
    Events.run(Trigger.draw) {
        func()
    }
}

inline fun RunOnPostDraw(crossinline func: () -> Unit) {
    Events.run(Trigger.postDraw) {
        func()
    }
}

inline fun RunOnPreDraw(crossinline func: () -> Unit) {
    Events.run(Trigger.preDraw) {
        func()
    }
}

inline fun Teamc.WhenTheSameTeam(func: () -> Unit): Boolean {
    if (team() == Vars.player.team()) {
        func()
        return true
    }
    return false
}