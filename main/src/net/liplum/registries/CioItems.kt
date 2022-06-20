package net.liplum.registries

import mindustry.type.Item
import net.liplum.ErekirSpec
import net.liplum.R
import net.liplum.VanillaSpec
import net.liplum.annotations.DependOn
import net.liplum.items.SpecItem

object CioItems {
    @JvmStatic lateinit var ic: Item
    @DependOn
    fun ic() {
        ic = SpecItem(R.I.IC, R.C.IcDark).apply {
            VanillaSpec {
                cost = 1.1f
                charge = 0.2f
            }
            ErekirSpec {
                cost = 1.2f
                charge = 0.5f
            }
        }
    }
}