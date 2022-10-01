package net.liplum.holo

import mindustry.type.Item
import mindustry.type.ItemStack
import mindustry.type.LiquidStack
import net.liplum.registry.CioFluid
import plumy.core.arc.Tick
import plumy.dsl.plus

class HoloPlan(
    val unitType: HoloUnitType,
    val req: HoloPlanRequirement,
) {
    val time: Tick get() = req.time
    val cyberion get() = req.cyberion
    val items get() = req.items
    val liquid get() = req.liquid
    val liquidArray get() = req.liquidArray
    operator fun contains(stack: ItemStack) = stack in req
    operator fun contains(item: Item) = item in req
}

class HoloPlanRequirement(
    val cyberion: Float = 0f,
    val items: Array<ItemStack> = emptyArray(),
    val time: Tick,
) {
    val liquid = CioFluid.cyberion + cyberion
    val liquidArray = arrayOf(liquid)
    operator fun contains(stack: ItemStack): Boolean {
        for (req in items)
            if (req == stack) return true
        return false
    }

    operator fun contains(item: Item): Boolean {
        for (req in items)
            if (req.item == item) return true
        return false
    }
}

val HoloPlan?.itemReqs: Array<ItemStack>
    get() = this?.items ?: ItemStack.empty
val HoloPlan?.cyberionReq: LiquidStack?
    get() = this?.liquid
