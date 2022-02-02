package net.liplum.blocks.virus

import arc.graphics.Color
import arc.math.Mathf
import arc.util.Tmp
import mindustry.Vars
import mindustry.gen.Building
import mindustry.graphics.Drawf
import mindustry.world.Block
import mindustry.world.Tile

open class AntiVirus(name: String) : Block(name) {
    var range: Float = 80f
    var uninfectedColor: Color = Color.green
    var infectedColor: Color = Color.red

    init {
        solid = true
        update = true
        hasPower = true
    }

    override fun canReplace(other: Block) = super.canReplace(other) || other is Virus
    override fun minimapColor(tile: Tile) = Color.green.rgba()
    override fun drawPlace(x: Int, y: Int, rotation: Int, valid: Boolean) {
        super.drawPlace(x, y, rotation, valid)
        Drawf.dashCircle(
            x * Vars.tilesize + offset,
            y * Vars.tilesize + offset,
            range, uninfectedColor
        )
        Vars.indexer.eachBlock(
            Vars.player.team(),
            x * Vars.tilesize + offset,
            y * Vars.tilesize + offset, range,
            {
                true
            }) {
            drawOtherInRange(it)
        }
    }

    private fun getOtherColor(other: Building): Color {
        return if (other is Virus.VirusBuild) infectedColor
        else uninfectedColor
    }

    private fun getOtherRenderColor(other: Building, temp: Color): Color {
        return temp.set(getOtherColor(other)).a(Mathf.absin(4f, 1f))
    }

    private fun drawOtherInRange(other: Building) {
        Drawf.selected(
            other,
            getOtherRenderColor(other, Tmp.c1)
        )
    }

    open inner class AntiVirusBuild : Building() {
        val realRange: Float
            get() = range * power.status * timeScale

        override fun updateTile() {
            Vars.indexer.eachBlock(this, realRange,
                { b ->
                    b is Virus.VirusBuild
                }) {
                it.tile.setAir()
            }
        }

        override fun drawSelect() {
            Vars.indexer.eachBlock(this, realRange,
                {
                    true
                }) {
                drawOtherInRange(it)
            }
            Drawf.dashCircle(x, y, realRange, uninfectedColor)
        }
    }
}