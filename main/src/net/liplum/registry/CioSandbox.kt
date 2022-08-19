package net.liplum.registry

import mindustry.type.Category
import mindustry.world.meta.BuildVisibility
import net.liplum.DebugOnly
import net.liplum.annotations.DependOn
import net.liplum.blocks.sandbox.AdjustableOverdrive
import plumy.core.Else

object CioSandbox {
    @JvmStatic lateinit var hyperOverdriveSphere: AdjustableOverdrive
    @DependOn
    fun hyperOverdriveSphere() {
        hyperOverdriveSphere = AdjustableOverdrive("hyper-overdrive-sphere").apply {
            category = Category.effect
            DebugOnly {
                buildVisibility = BuildVisibility.shown
            }.Else {
                buildVisibility = BuildVisibility.sandboxOnly
            }
            requirements = emptyArray()
            size = 3
            maxBoost = 50f
            minBoost = 0.5f
            speedBoost = 50f
            range = 1000f
        }
    }
}