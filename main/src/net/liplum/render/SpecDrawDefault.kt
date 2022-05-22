package net.liplum.render

import arc.graphics.g2d.Draw
import arc.util.Eachable
import mindustry.entities.units.BuildPlan
import mindustry.gen.Building
import mindustry.world.Block
import mindustry.world.draw.DrawBlock
import net.liplum.lib.EmptyTR
import net.liplum.lib.TRs
import net.liplum.utils.atlasX

class SpecDrawDefault : DrawBlock() {
    var tr = EmptyTR
    override fun load(block: Block) = block.run {
        tr = block.atlasX()
    }

    override fun draw(build: Building) {
        Draw.rect(tr, build.x, build.y, build.drawrot())
    }

    override fun drawPlan(block: Block, plan: BuildPlan?, list: Eachable<BuildPlan?>?) {
        block.drawDefaultPlanRegion(plan, list)
    }

    override fun icons(block: Block): TRs {
        return arrayOf(tr)
    }
}