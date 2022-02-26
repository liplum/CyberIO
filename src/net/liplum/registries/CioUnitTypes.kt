package net.liplum.registries

import mindustry.ctype.ContentList
import mindustry.type.UnitType

class CioUnitTypes : ContentList {
    companion object {
        @JvmStatic
        lateinit var holoUnit: UnitType
    }

    override fun load() {
    }
}