package net.liplum.mdt.animations.ganim

import net.liplum.mdt.ClientOnly

interface IGlobalAnimation {
    val canUpdate: Boolean
    @ClientOnly
    fun update()
}