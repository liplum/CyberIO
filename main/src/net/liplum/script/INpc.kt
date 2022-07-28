package net.liplum.script

import net.liplum.mdt.ClientOnly

interface INpc {
    @ClientOnly
    fun showDialog()
}