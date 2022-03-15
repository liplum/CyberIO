package net.liplum.utils

import mindustry.Vars
import mindustry.world.Block

fun Short.toDrawXY(block: Block): Float {
    return block.offset + this * Vars.tilesize
}

fun Int.toDrawXY(block: Block): Float {
    return block.offset + this * Vars.tilesize
}

val Short.toDrawXY: Float
    get() = this.toFloat() * Vars.tilesize
val Int.drawXY: Float
    get() = this.toFloat() * Vars.tilesize
