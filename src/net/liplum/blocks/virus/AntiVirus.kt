package net.liplum.blocks.virus

import arc.graphics.Color
import mindustry.Vars
import mindustry.gen.Building
import mindustry.graphics.Drawf
import mindustry.world.Block
import mindustry.world.Tile
import mindustry.world.meta.BlockGroup
import net.liplum.utils.G
import net.liplum.utils.WorldUtil

open class AntiVirus(name: String) : Block(name) {
    var range: Float = 80f
    var uninfectedColor: Color = Color.green
    var infectedColor: Color = Color.red

    init {
        solid = true
        update = true
        group = BlockGroup.projectors
        hasPower = true
    }

    override fun canReplace(other: Block) = super.canReplace(other) || other is Virus
    override fun minimapColor(tile: Tile) = Color.green.rgba()
    override fun drawPlace(x: Int, y: Int, rotation: Int, valid: Boolean) {
        super.drawPlace(x, y, rotation, valid)
        G.drawDashCircle(this, x, y, range, uninfectedColor)
        Vars.indexer.eachBlock(
            Vars.player.team(),
            WorldUtil.toDrawXY(this, x),
            WorldUtil.toDrawXY(this, y),
            range,
            {
                true
            }) {
            G.drawSelected(it, getOtherColor(it))
        }
    }

    open fun getOtherColor(other: Building): Color {
        return if (other is IVirusBuilding) infectedColor
        else uninfectedColor
    }

    open inner class AntiVirusBuild : Building() {
        val realRange: Float
            get() = range * efficiency() * timeScale

        override fun updateTile() {
            Vars.indexer.eachBlock(this, realRange,
                { b ->
                    b is IVirusBuilding
                }) {
                (it as? IVirusBuilding)?.killVirus()
            }
        }

        override fun drawSelect() {
            Vars.indexer.eachBlock(this, realRange,
                {
                    true
                }) {
                G.drawSelected(it, getOtherColor(it))
            }
            Drawf.dashCircle(x, y, realRange, uninfectedColor)
        }
    }
}