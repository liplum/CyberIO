package net.liplum.registries

import net.liplum.annotations.DependOn
import net.liplum.mdt.animations.ganim.GlobalAnimation.Companion.randomSelectIndexr
import net.liplum.seffects.Infected
import net.liplum.seffects.Static
import net.liplum.seffects.Vulnerable
import net.liplum.utils.globalAnim

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