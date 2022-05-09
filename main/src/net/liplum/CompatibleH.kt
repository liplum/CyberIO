package net.liplum

import net.liplum.Compatible.*

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