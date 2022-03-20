package net.liplum.blocks.cyberion

import mindustry.world.blocks.production.LiquidConverter

open class CyberionMixer(name: String) : LiquidConverter(name) {
    init {
        hasPower = true
        hasItems = true
        outputsLiquid = true
    }
    open inner class CyberionBuild : LiquidConverterBuild() {
    }
}