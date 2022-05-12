package net.liplum.registries

import mindustry.type.Item
import net.liplum.R
import net.liplum.annotations.DependOn

object CioItems {
    @JvmStatic lateinit var ic: Item
    @DependOn
    fun ic() {
        ic = Item(R.I.IC, R.C.IcDark)
    }
}