package net.liplum.script

import plumy.core.ClientOnly

interface INpc {
    @ClientOnly
    fun showDialog()
}