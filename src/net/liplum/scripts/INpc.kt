package net.liplum.scripts

import net.liplum.ClientOnly

interface INpc {
    @ClientOnly
    fun showDialog()
}