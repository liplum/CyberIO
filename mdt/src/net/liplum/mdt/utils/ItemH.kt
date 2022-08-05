package net.liplum.mdt.utils

import arc.graphics.Color
import mindustry.ctype.UnlockableContent
import mindustry.type.*

operator fun Item.plus(amount: Int) =
    ItemStack(this, amount)

operator fun Liquid.plus(amount: Float) =
    LiquidStack(this, amount)

operator fun ItemStack.plusAssign(amount: Int) {
    this.amount += amount
}

operator fun LiquidStack.plusAssign(amount: Float) {
    this.amount += amount
}

val Liquid.fluidColor: Color
    get() = if (gas) gasColor else color

operator fun UnlockableContent.plus(amount: Int) =
    PayloadStack(this, amount)

operator fun PayloadStack.plusAssign(amount: Int) {
    this.amount += amount
}

