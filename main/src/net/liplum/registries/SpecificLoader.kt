package net.liplum.registries

import net.liplum.CioMod
import net.liplum.ContentSpec
import net.liplum.R
import net.liplum.S

object SpecificLoader {
    @JvmStatic
    fun handle() {
        when (CioMod.ContentSpecific) {
            ContentSpec.Erekir -> {
                S.Hologram = R.C.HoloOrange
                S.HologramDark =  R.C.HoloDarkOrange
            }
            else -> {
            }
        }
    }
}