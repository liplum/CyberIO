package net.liplum.registries

import mindustry.type.Item
import net.liplum.R
import net.liplum.annotations.DependOn
import net.liplum.items.SpecItem

object CioItems {
    @JvmStatic lateinit var ic: Item
    @DependOn
    fun ic() {
        ic = SpecItem(R.I.IC, R.C.IcDark)
    }
}