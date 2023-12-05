package net.liplum.render

import mindustry.world.Block
import mindustry.world.draw.*
import net.liplum.common.util.StartWithHyphen
import net.liplum.spec
import net.liplum.utils.or
import net.liplum.utils.spriteX

fun DrawRegionSpec(
    suffix: String = "",
) = DrawRegion(suffix.spec)

fun DrawHeatRegionSpec(
    suffix: String = "-glow",
) = DrawHeatRegion(suffix.spec)

fun DrawGlowRegionSpec(
    suffix: String = "-glow",
) = DrawGlowRegion(suffix.spec)

fun DrawHeatInputSpec(
    suffix: String = "-heat",
) = DrawHeatInput(suffix.spec)

fun DrawConstructSpec(
    stages: Int = 3,
    @StartWithHyphen
    suffix: String = "construct",
) = DrawConstruct(stages, suffix.spec)

class DrawDefaultSpec : DrawDefault() {
    override fun load(block: Block) = block.run {
        region = this.spriteX
    }

    override fun icons(block: Block) = arrayOf(block.region)
}

class DrawHeatOutputSpec : DrawHeatOutput() {
    override fun load(block: Block) = block.run {
        heat = "$name-heat".spriteX
        glow = "$name-glow".spriteX
        top1 = "$name-top1".spriteX
        top2 = "$name-top2".spriteX
    }
}

class DrawPistonsSpec : DrawPistons() {
    override fun load(block: Block) = block.run {
        region1 = "$name-piston0".spriteX or "$name-piston".spriteX
        region2 = "$name-piston1".spriteX or "$name-piston".spriteX
        regiont = "$name-piston-t".spriteX
        iconRegion = "$name-icon".spriteX
    }
}