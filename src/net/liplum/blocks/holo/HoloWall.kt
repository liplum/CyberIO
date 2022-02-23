package net.liplum.blocks.holo

import mindustry.world.blocks.defense.Wall

open class HoloWall(name: String) : Wall(name) {
    open inner class HoloBuild : WallBuild() {
        override fun updateTile() {
            super.updateTile()
        }
    }
}