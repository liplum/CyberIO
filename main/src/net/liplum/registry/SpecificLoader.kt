package net.liplum.registry

import net.liplum.*

object SpecificLoader {
    @JvmStatic
    fun handle() {
        when (Var.ContentSpecific) {
            ContentSpec.Erekir -> {
                Var.Hologram = R.C.HoloOrange
                Var.HologramDark = R.C.HoloDarkOrange
            }
            else -> {
            }
        }
    }
}