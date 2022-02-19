package net.liplum

import mindustry.Vars

annotation class ClientOnly
annotation class V6
annotation class V7

inline fun ClientOnly(func: () -> Unit) {
    if (!Vars.headless) {
        func()
    }
}

inline fun ServerOnly(func: () -> Unit) {
    if (Vars.headless) {
        func()
    }
}

inline fun CanGlobalAnimationPlay(func: () -> Unit) {
    if (CioMod.CanGlobalAnimationPlay) {
        func()
    }
}

inline fun CanAniStateLoad(func: () -> Unit) {
    if (CioMod.CanAniStateLoad) {
        func()
    }
}

inline fun DebugOnly(func: () -> Unit) {
    if (CioMod.DebugMode) {
        func()
    }
}