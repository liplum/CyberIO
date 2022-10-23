package net.liplum.consumer

import mindustry.gen.Building
import mindustry.type.Liquid
import mindustry.world.consumers.ConsumeLiquidFilter
import mindustry.world.meta.Stats

class LiquidTurretCons(
    vararg val types: Liquid,
) : ConsumeLiquidFilter({ it in types }, 1f) {
    override fun update(build: Building) {}
    override fun display(stats: Stats) {}
}