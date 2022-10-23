package net.liplum.render

import arc.graphics.g2d.Draw
import mindustry.gen.Building
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import mindustry.graphics.Pal
import mindustry.world.Block
import mindustry.world.draw.DrawBlock
import net.liplum.common.util.StartWithHyphen
import plumy.core.assets.EmptyTRs
import net.liplum.utils.sheet

class DrawConstruct(
    var stages: Int = 3,
    @StartWithHyphen
    var suffix: String = "construct",
) : DrawBlock() {
    var stageRegions = EmptyTRs
    override fun draw(build: Building) {
        val stage = (build.progress() * stages).toInt()
        val stageProgress = (build.progress() * stages) % 1f

        for (i in 0 until stage) {
            Draw.rect(stageRegions[i], build.x, build.y)
        }

        Draw.draw(Layer.blockOver) {
            Drawf.construct(
                build,
                stageRegions[stage],
                Pal.accent,
                0f,
                stageProgress,
                build.warmup() * build.efficiency(),
                build.totalProgress() * 1.6f
            )
        }
    }

    override fun load(block: Block) {
        stageRegions = block.sheet(suffix, stages)
    }
}