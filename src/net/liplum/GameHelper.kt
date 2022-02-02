package net.liplum

import mindustry.Vars

class GameHelper {
    companion object {
        @JvmStatic
        fun ClientOnly(func: () -> Unit) {
            if (!Vars.headless) {
                func()
            }
        }
        @JvmStatic
        fun ServerOnly(func: () -> Unit) {
            if (Vars.headless) {
                func()
            }
        }
    }
}