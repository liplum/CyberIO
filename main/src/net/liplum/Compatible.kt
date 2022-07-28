package net.liplum

import mindustry.Vars
import net.liplum.Compatible.TvStatic

object Compatible {
    @JvmField var Hologram = false
    @JvmField var TvStatic = Vars.mobile
    @JvmField var Monochromize = Vars.mobile
    @JvmField var InvertingColorRgb = Vars.mobile
}

val CompatibleMap = mapOf(
    "TvStatic" to { TvStatic },
)
val String.useCompatible: Boolean
    get() {
        val getter = CompatibleMap[this]
        if (getter != null) {
            return getter()
        }
        return false
    }
typealias Cptb = Compatible