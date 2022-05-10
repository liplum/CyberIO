package net.liplum.lib.mixin

import mindustry.world.modules.LiquidModule

private val totalLiquidCalcu = LiquidModule.LiquidCalculator { _, amount ->
    amount
}

fun LiquidModule.total(): Float {
    return sum(totalLiquidCalcu)
}