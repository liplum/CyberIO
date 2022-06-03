package net.liplum.registries

import net.liplum.*

object SpecificLoader {
    @JvmStatic
    fun handle() {
        when (Var.ContentSpecific) {
            ContentSpec.Erekir -> {
                S.Hologram = R.C.HoloOrange
                S.HologramDark =  R.C.HoloDarkOrange
            }
            else -> {
            }
        }
    }
}