package net.liplum.registries

import net.liplum.seffects.Infected
import net.liplum.seffects.Vulnerable

class CioStatusEffects : ContentTable {
    companion object {
        @JvmStatic lateinit var infected: Infected
        @JvmStatic lateinit var vulnerable: Vulnerable
    }

    override fun firstLoad() {
    }

    override fun load() {
        infected = Infected("infected")
        vulnerable = Vulnerable("vulnerable")
    }

    override fun lastLoad() {
    }
}