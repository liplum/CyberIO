package net.liplum.render

import mindustry.world.draw.DrawRegion
import net.liplum.mdt.render.DrawConstruct
import net.liplum.spec

fun DrawRegionSpec(
    suffix: String = "",
) = DrawRegion(suffix.spec)

fun SpecDrawConstruct(
    stages: Int = 3,
    suffix: String = "",
) = DrawConstruct(stages, suffix.spec)