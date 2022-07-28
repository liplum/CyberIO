package net.liplum.mdt.animation.ganim

import net.liplum.mdt.ClientOnly

interface IGlobalAnimation {
    val canUpdate: Boolean
    @ClientOnly
    fun update()
}