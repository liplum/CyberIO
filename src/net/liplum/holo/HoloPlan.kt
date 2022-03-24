package net.liplum.holo

import mindustry.type.ItemStack
import mindustry.type.LiquidStack
import net.liplum.registries.CioLiquids

open class HoloPlan(
    val unitType: HoloUnitType,
    val req: Requirement,
    val time: Float
)

open class Requirement(
    val items: Array<ItemStack>,
    val cyberionReq: Float,
) {
    constructor(
        cyberionReq: Float
    ) : this(emptyArray(), cyberionReq)

    constructor(
        items: Array<ItemStack>
    ) : this(items, 0f)

    val liquids = if (cyberionReq != 0f)
        arrayOf(LiquidStack(CioLiquids.cyberion, cyberionReq))
    else
        arrayOf()
}

val HoloPlan?.itemReqs: Array<ItemStack>
    get() {
        if (this == null) {
            return ItemStack.empty
        }
        return this.req.items
    }
val emptyCyberionLiquidStack: LiquidStack by lazy {
    LiquidStack(CioLiquids.cyberion, 0f)
}
val emptyCyberionLiquidStackArray: Array<LiquidStack> = arrayOf()
val HoloPlan?.cyberionReq: Array<LiquidStack>
    get() {
        if (this == null) {
            return emptyCyberionLiquidStackArray
        }
        return req.liquids
    }
