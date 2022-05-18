package net.liplum.scripts

import net.liplum.mdt.ClientOnly

interface INpc {
    @ClientOnly
    fun showDialog()
}