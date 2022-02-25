package net.liplum

import arc.util.Time
import mindustry.Vars
import net.liplum.utils.AniU
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
fun Float.toSeconds(digits: Int): String = (this / Time.toSeconds).format(digits)