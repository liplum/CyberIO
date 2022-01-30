package net.liplum.registries

import arc.graphics.Color
import mindustry.ctype.ContentList
import mindustry.type.Item
import net.liplum.R

class CioItems : ContentList {
    companion object {
        @JvmStatic
        lateinit var ic: Item
    }

    override fun load() {
        ic = Item(R.I.IC, Color.valueOf("272727"))
    }
}