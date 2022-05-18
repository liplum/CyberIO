package net.liplum.mdt.render

import arc.Core
import arc.graphics.g2d.Draw
import arc.graphics.g2d.TextureRegion
import mindustry.gen.Building
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import mindustry.graphics.Pal
import mindustry.world.Block
import mindustry.world.draw.DrawBlock

class DrawConstruct : DrawBlock(){
    var stages = 3
    //TODO: use `var stageRegions :TRs = emptyArray()` instead of nullable array
    var stageRegions = Array<TextureRegion?> (stages) { null }
    // TODO: use `TR` instead of this long name
    lateinit var topRegion: TextureRegion

    override fun draw(build: Building) {
        val stage = (build.progress() * stages).toInt()

        for(i in 0 until stage) {
            Draw.rect(stageRegions[i], build.x, build.y)
        }
        Draw.draw(Layer.blockOver) {
            Drawf.construct(
                build,
                stageRegions[stage],
                Pal.accent,
                0f,
                (build.progress() * stages) % 1f,
                build.warmup() * build.efficiency(),
                build.totalProgress() * 1.6f * build.efficiency()
            )
        }
        // TODO: Top is important,so it must be found. Otherwise it indicates the mod is wrong and please show this error.
        if (topRegion.found()) Draw.rect(topRegion, build.x, build.y)
    }

/* TODO: Use `Any.run{}` make a inline closure. Now you can access any fields as inside
    override fun draw(build: Building)  = build.run{
        val stage = (progress() * stages).toInt()

        for(i in 0 until stage) {
            Draw.rect(stageRegions[i], x, y)
        }
        Draw.draw(Layer.blockOver) {
            Drawf.construct(this, stageRegions[stage], Pal.accent, 0f, (progress() * stages) % 1f, warmup() * efficiency(), totalProgress() * 1.6f * build.efficiency())
        }
        if (topRegion.found()) Draw.rect(topRegion, x, y)
    }
*/

    override fun load(block: Block) {
        val name = block.name
        // TODO: Use `block.sub("top")` instead of this Core.atlas.find
        topRegion = Core.atlas.find("$name-top")
        // TODO: Use `block.subFrames("construct",3)` instead of this loop
        for(i in 0 until stages) {
            stageRegions[i] = Core.atlas.find("$name-construct$i")
        }
    }
}