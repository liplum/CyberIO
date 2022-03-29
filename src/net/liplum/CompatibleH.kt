package net.liplum

import net.liplum.Compatible.*

val CompatibleMap = mapOf(
    "Hologram" to { Hologram },
    "TvStatic" to { TvStatic },
    "Monochromize" to { Monochromize },
    "InvertingColorRgb" to { InvertingColorRgb },
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