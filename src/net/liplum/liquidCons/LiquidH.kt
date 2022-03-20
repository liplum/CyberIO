package net.liplum.liquidCons

import mindustry.type.Liquid
import mindustry.type.LiquidStack
import mindustry.world.modules.LiquidModule

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
