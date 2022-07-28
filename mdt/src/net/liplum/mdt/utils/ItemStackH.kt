package net.liplum.mdt.utils

import mindustry.ctype.UnlockableContent
import mindustry.type.*

operator fun Item.plus(amount: Int) =
    ItemStack(this, amount)

operator fun Liquid.plus(amount: Float) =
    LiquidStack(this, amount)

operator fun UnlockableContent.plus(amount: Int) =
    PayloadStack(this, amount)
