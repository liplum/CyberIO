package net.liplum.brains

import mindustry.gen.Building
import mindustry.world.Block

open class Heimdall(name: String) : Block(name) {
    init {
        solid = true
        update = true
    }

    open inner class HeimdallBuild : Building() {
        override fun updateTile() {

        }
    }
}