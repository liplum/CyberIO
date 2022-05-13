package net.liplum.registries

import net.liplum.annotations.DependOn
import net.liplum.lib.animations.ganim.globalAnim
import net.liplum.seffects.Infected
import net.liplum.seffects.Static
import net.liplum.seffects.Vulnerable

object CioSEffects {
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