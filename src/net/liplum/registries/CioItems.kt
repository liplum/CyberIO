package net.liplum.registries

import mindustry.type.Item
import net.liplum.R

object CioItems : ContentTable {
    @JvmStatic lateinit var ic: Item
    override fun load() {
    }

    override fun firstLoad() {
        ic = Item(R.I.IC, R.C.IcDark)
    }

    override fun lastLoad() {
    }
}