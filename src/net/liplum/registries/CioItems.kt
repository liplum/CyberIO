package net.liplum.registries

import mindustry.type.Item
import net.liplum.R

class CioItems : ContentTable {
    companion object {
        @JvmStatic lateinit var ic: Item
    }

    override fun load() {
    }

    override fun firstLoad() {
        ic = Item(R.I.IC, R.C.IcDark)
    }

    override fun lastLoad() {
    }
}