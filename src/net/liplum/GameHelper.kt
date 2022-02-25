package net.liplum

import arc.util.Time
import mindustry.Vars
import net.liplum.utils.AniU
import net.liplum.utils.format

annotation class ClientOnly
/**
 * Runs codes only on Physical Client
 */
inline fun ClientOnly(func: () -> Unit) {
    if (!Vars.headless) {
        func()
    }
}
/**
 * Runs codes only on Logical Server
 */
inline fun ServerOnly(func: () -> Unit) {
    val net = Vars.net
    if (net.server() || !net.active()) {
        func()
    }
}

inline fun WhenCanGlobalAnimationPlay(func: () -> Unit) {
    if (CioMod.CanGlobalAnimationPlay) {
        func()
    }
}

inline fun WhenCanAniStateLoad(func: () -> Unit) {
    if (CioMod.CanAniStateLoad && AniU.needUpdateAniStateM()) {
        func()
    }
}

inline fun DebugOnly(func: () -> Unit) {
    if (CioMod.DebugMode) {
        func()
    }
}

fun Float.toSecond(): Int = (this / Time.toSeconds).toInt()
fun Float.toSeconds(digits:Int): String = (this / Time.toSeconds).format(digits)