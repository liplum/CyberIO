package net.liplum.blocks.decentralizer

import mindustry.gen.Building
import mindustry.world.Block

class Decentralizer(name: String) : Block(name) {
    var acceptInputTime = 3 * 60f
    var acceptInputReload = 3 * 60f

    init {
        solid = true
        update = true
    }

    inner class DecentralizerBuild : Building() {
        var acceptInputTimer = 0f
        var acceptInputCounter = 0f
        override fun updateTile() {

        }
    }
}