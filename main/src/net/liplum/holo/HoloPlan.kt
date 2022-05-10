package net.liplum.holo

import mindustry.type.ItemStack
import mindustry.type.LiquidStack
import net.liplum.registries.CioLiquids

open class HoloPlan(
    val unitType: HoloUnitType,
    val req: Requirement,
    val time: Float,
)

open class Requirement(
    val items: Array<ItemStack>,
    val cyberionReq: Float,
) {
    constructor(
        cyberionReq: Float,
    ) : this(emptyArray(), cyberionReq)

    constructor(
        items: Array<ItemStack>,
    ) : this(items, 0f)

    val liquid = if (cyberionReq != 0f)
        LiquidStack(CioLiquids.cyberion, cyberionReq)
    else
        null
}

val HoloPlan?.itemReqs: Array<ItemStack>
    get() = this?.req?.items ?: ItemStack.empty
val HoloPlan?.cyberionReq: LiquidStack?
    get() = this?.req?.liquid
