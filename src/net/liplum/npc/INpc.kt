package net.liplum.npc

import net.liplum.ClientOnly

interface INpc {
    @ClientOnly
    fun showDialog()
}