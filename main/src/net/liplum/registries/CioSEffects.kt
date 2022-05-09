package net.liplum.registries

import net.liplum.lib.animations.ganim.globalAnim
import net.liplum.seffects.Infected
import net.liplum.seffects.Static
import net.liplum.seffects.Vulnerable

object CioSEffects : ContentTable {
    @JvmStatic lateinit var infected: Infected
    @JvmStatic lateinit var vulnerable: Vulnerable
    @JvmStatic lateinit var static: Static
    override fun firstLoad() {
    }

    override fun load() {
        infected = Infected("infected")
        vulnerable = Vulnerable("vulnerable")
        static = Static("static").globalAnim(
            20f, 5
        )
    }

    override fun lastLoad() {
    }
}