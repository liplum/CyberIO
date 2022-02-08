package net.liplum

import mindustry.Vars

class GameHelper {
    companion object {
        @JvmStatic
        inline fun ClientOnly(func: () -> Unit) {
            if (!Vars.headless) {
                func()
            }
        }
        @JvmStatic
        inline fun ServerOnly(func: () -> Unit) {
            if (Vars.headless) {
                func()
            }
        }
        @JvmStatic
        inline fun CanGlobalAnimationPlay(func: () -> Unit) {
            if (CioMod.CanGlobalAnimationPlay) {
                func()
            }
        }
        @JvmStatic
        inline fun CanAniStateLoad(func: () -> Unit) {
            if (CioMod.CanAniStateLoad) {
                func()
            }
        }
    }
}