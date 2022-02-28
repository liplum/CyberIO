package net.liplum.registries

import net.liplum.seffects.Infected

class CioStatusEffects : ContentTable {
    companion object {
        @JvmStatic lateinit var infected: Infected
    }

    override fun firstLoad() {
    }

    override fun load() {
        infected = Infected("infected")
    }

    override fun lastLoad() {
    }
}