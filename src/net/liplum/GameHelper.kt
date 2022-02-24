package net.liplum

import mindustry.Vars
import net.liplum.utils.AniU

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