package net.liplum.render

import arc.Core
import mindustry.world.Block
import mindustry.world.draw.*
import net.liplum.common.util.StartWithHyphen
import net.liplum.mdt.render.DrawConstruct
import net.liplum.spec
import net.liplum.util.atlasX

fun DrawRegionSpec(
    suffix: String = "".spec,
) = DrawRegion(suffix.spec)

fun DrawHeatRegionSpec(
    suffix: String = "-glow".spec,
) = DrawHeatRegion(suffix.spec)

fun DrawGlowRegionSpec(
    suffix: String = "-glow".spec,
) = DrawGlowRegion(suffix.spec)

fun DrawHeatInputSpec(
    suffix: String = "",
) = DrawHeatInput(suffix.spec)

fun DrawConstructSpec(
    stages: Int = 3,
    @StartWithHyphen
    suffix: String = "construct".spec,
) = DrawConstruct(stages, suffix.spec)

class DrawDefaultSpec : DrawDefault() {
    override fun load(block: Block) = block.run {
        region = this.atlasX()
    }

    override fun icons(block: Block) = arrayOf(block.region)
}

class DrawHeatOutputSpec : DrawHeatOutput() {
    override fun load(block: Block) = block.run {
        heat = Core.atlas.find("$name-heat".spec)
        glow = Core.atlas.find("$name-glow".spec)
        top1 = Core.atlas.find("$name-top1".spec)
        top2 = Core.atlas.find("$name-top2".spec)
    }
}