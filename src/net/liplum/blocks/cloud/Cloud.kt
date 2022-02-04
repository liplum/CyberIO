package net.liplum.blocks.cloud

import mindustry.game.Team
import mindustry.gen.Building
import mindustry.world.Block

open class Cloud(name: String) : Block(name) {
    init {
        solid = true
        update = true
    }

    open inner class CloudBuild : Building() {
        override fun create(block: Block?, team: Team?): Building {
            return super.create(block, team)
        }
    }
}