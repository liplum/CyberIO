package net.liplum.lib.animations.ganim

import net.liplum.ClientOnly

interface IGlobalAnimation {
    val canUpdate: Boolean
    @ClientOnly
    fun update()
}