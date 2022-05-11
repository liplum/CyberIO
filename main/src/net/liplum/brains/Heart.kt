package net.liplum.brains

import arc.graphics.g2d.TextureRegion
import mindustry.content.UnitTypes
import mindustry.gen.BlockUnitc
import mindustry.gen.Building
import mindustry.world.Block
import mindustry.world.blocks.ControlBlock
import mindustry.world.draw.DrawMulti
import mindustry.world.draw.DrawRegion
import net.liplum.api.brain.*
import net.liplum.utils.MdtUnit

class Heart(name: String) : Block(name), IComponentBlock {
    override val upgrades: MutableMap<UpgradeType, Upgrade> = HashMap()

    init {
        solid = true
        update = true
        hasPower = true
        sync = true
        canOverdrive = false
    }

    var drawer = DrawMulti(
        DrawRegion("-base"),
        DrawRegion("-heart"),
    )
    override fun load() {
        super.load()
        drawer.load(this)
    }

    override fun setStats() {
        super.setStats()
        this.addUpgradeComponentStats()
    }

    override fun icons(): Array<TextureRegion> {
        return drawer.icons(this)
    }
    open inner class HeartBuild : Building(), IUpgradeComponent, ControlBlock {
        override var directionInfo: Direction2 = Direction2()
        override var brain: IBrain? = null
        override val upgrades: Map<UpgradeType, Upgrade>
            get() = this@Heart.upgrades
        var unit = UnitTypes.block.create(team) as BlockUnitc
        override fun unit(): MdtUnit {
            //make sure stats are correct
            unit.tile(this)
            unit.team(team)
            return (unit as MdtUnit)
        }

        override fun draw() {
            drawer.draw(this)
        }
    }
}