package net.liplum.registry

import net.liplum.annotations.DependOn
import net.liplum.statusFx.Infected
import net.liplum.statusFx.Static
import net.liplum.statusFx.Vulnerable
import net.liplum.utils.globalAnim

object CioSEffect {
    @JvmStatic lateinit var infected: Infected
    @JvmStatic lateinit var vulnerable: Vulnerable
    @JvmStatic lateinit var static: Static
    @DependOn
    fun infected() {
        infected = Infected("infected")
    }
    @DependOn
    fun vulnerable() {
        vulnerable = Vulnerable("vulnerable")
    }
    @DependOn
    fun static() {
        static = Static("static").globalAnim(
            20f, 5
        )
    }
}