package net.liplum.registries

import mindustry.type.Liquid
import net.liplum.DebugOnly
import net.liplum.R

object CioLiquids : ContentTable {
    @JvmStatic
    lateinit var cyberion: Liquid
    override fun firstLoad() {
        DebugOnly {
            cyberion = Liquid(R.Liquid.Cyberion, R.C.Holo)
        }
    }

    override fun load() {
    }

    override fun lastLoad() {
    }
}