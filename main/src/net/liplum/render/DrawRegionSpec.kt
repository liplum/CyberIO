package net.liplum.render

import mindustry.world.draw.DrawRegion
import net.liplum.Var
import net.liplum.suffixResource

fun DrawRegionSpec(
    suffix: String = "",
) = DrawRegion(Var.ContentSpecific.suffixResource(suffix))