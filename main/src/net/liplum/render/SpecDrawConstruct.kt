package net.liplum.render

import arc.graphics.g2d.Draw
import mindustry.gen.Building
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import mindustry.graphics.Pal
import mindustry.world.Block
import mindustry.world.draw.DrawBlock
import net.liplum.lib.assets.TR
import net.liplum.lib.assets.TRs
import net.liplum.util.sheetX
import net.liplum.util.subX

class SpecDrawConstruct : DrawBlock() {
    var stages = 3
    lateinit var stageRegions: TRs
    lateinit var topRegion: TR
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
                build.totalProgress() * 1.6f * build.efficiency()
            )
        }

        if (topRegion.found()) Draw.rect(topRegion, build.x, build.y)
    }

    override fun load(block: Block) {
        topRegion = block.subX("top")
        stageRegions = block.sheetX("construct", stages)
    }
}