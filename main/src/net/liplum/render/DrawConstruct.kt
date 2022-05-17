package net.liplum.render

import arc.Core
import arc.graphics.g2d.Draw
import arc.graphics.g2d.TextureRegion
import arc.math.Mathf
import mindustry.gen.Building
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import mindustry.graphics.Pal
import mindustry.world.Block
import mindustry.world.draw.DrawBlock
import kotlin.math.roundToInt

class DrawConstruct : DrawBlock(){
    var stages = 3
    var stageRegions = Array<TextureRegion?> (stages) { null }
    lateinit var topRegion: TextureRegion

    override fun draw(build: Building) {
        val stage = (build.progress() * stages).toInt()

        for(i in 0 until stage) {
            Draw.rect(stageRegions[i], build.x, build.y)
        }
        Draw.draw(Layer.blockOver) {
            Drawf.construct(build, stageRegions[stage], Pal.accent, 0f, (build.progress() * stages) % 1f, build.warmup() * build.efficiency(), build.totalProgress() * 1.6f * build.efficiency())
        }
        if (topRegion.found()) Draw.rect(topRegion, build.x, build.y)
    }

    override fun load(block: Block) {
        val name = block.name

        topRegion = Core.atlas.find("$name-top")
        for(i in 0 until stages) {
            stageRegions[i] = Core.atlas.find("$name-construct$i")
        }
    }
}