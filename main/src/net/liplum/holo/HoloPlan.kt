package net.liplum.holo

import mindustry.type.Item
import mindustry.type.ItemStack
import mindustry.type.LiquidStack
import plumy.core.arc.Tick
import net.liplum.mdt.utils.plus
import net.liplum.registry.CioFluid

open class HoloPlan(
    val unitType: HoloUnitType,
    val req: Requirement,
    val time: Tick,
)

open class Requirement(
    val cyberion: Float = 0f,
    val items: Array<ItemStack> = emptyArray(),
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
    get() = this?.req?.items ?: ItemStack.empty
val HoloPlan?.cyberionReq: LiquidStack?
    get() = this?.req?.liquid
