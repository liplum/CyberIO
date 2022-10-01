package net.liplum.holo

import arc.func.Prov
import arc.util.Time
import mindustry.Vars
import mindustry.gen.Building
import mindustry.world.Block
import mindustry.world.meta.BlockGroup
import net.liplum.registry.CioBlock

open class LandProjector(name: String) : Block(name) {
    private val projectRadius = 10

    init {
        buildType = Prov { LandProjectorBuild() }
        solid = true
        group = BlockGroup.projectors
        update = true
    }

    open inner class LandProjectorBuild : Building() {
        override fun updateTile() {
            val transform = Time.time % 60f < 1
            if (transform) {
                val selfBlockX = tile().x.toInt()
                val selfBlockY = tile().y.toInt()
                val projectRadius = projectRadius + size % 2
                val xm = selfBlockX - projectRadius //x minus
                val xp = selfBlockX + projectRadius //x plus
                val ym = selfBlockY - projectRadius //y minus
                val yp = selfBlockY + projectRadius //y plus
                for (i in xm until xp) {
                    for (j in ym until yp) {
                        val tile = Vars.world.tile(i, j)
                        if (tile != null && tile.floor() !is HoloFloor) {
                            // v6 doesn't support floor dynamically changing
                            tile.setFloorUnder(CioBlock.holoFloor)
                        }
                    }
                }
            }
        }
    }
}