package net.liplum.mixin

import mindustry.type.Liquid
import mindustry.type.LiquidStack
import mindustry.world.modules.LiquidModule

private val totalLiquidCalc = LiquidModule.LiquidCalculator { _, amount ->
    amount
}

fun LiquidModule.total(): Float {
    return sum(totalLiquidCalc)
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

fun LiquidModule.remove(reqs: Array<LiquidStack>, multiplier: Float) {
    for (req in reqs) {
        remove(req.liquid, req.amount * multiplier)
    }
}