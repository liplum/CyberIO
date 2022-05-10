package net.liplum.lib.mixin

import mindustry.type.Liquid
import mindustry.type.LiquidStack
import mindustry.world.modules.LiquidModule

private val totalLiquidCalcu = LiquidModule.LiquidCalculator { _, amount ->
    amount
}

fun LiquidModule.total(): Float {
    return sum(totalLiquidCalcu)
}

fun LiquidModule.has(liquid: Liquid, amount: Float) =
    this[liquid] >= amount

fun LiquidModule.has(liquidStack: LiquidStack) =
    this[liquidStack.liquid] >= liquidStack.amount

fun LiquidModule.has(liquidStacks: Array<LiquidStack>): Boolean {
    for (stack in liquidStacks) {
        if (!this.has(stack)) {
            return false
        }
    }
    return true
}
