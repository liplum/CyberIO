package net.liplum.utils

import mindustry.Vars
import mindustry.gen.Building
import mindustry.world.Block
import mindustry.world.Tile

fun Short.toDrawXY(block: Block): Float =
    block.offset + this * Vars.tilesize

fun Int.toDrawXY(block: Block): Float =
    block.offset + this * Vars.tilesize

val Short.toDrawXY: Float
    get() = this.toFloat() * Vars.tilesize
val Int.drawXY: Float
    get() = this.toFloat() * Vars.tilesize
@JvmOverloads
fun Tile.left(distance: Int = 1): Tile? {
    return Vars.world.tile(x.toInt() - distance, y.toInt())
}
@JvmOverloads
fun Tile.right(distance: Int = 1): Tile? {
    return Vars.world.tile(x.toInt() + distance, y.toInt())
}
@JvmOverloads
fun Tile.bottom(distance: Int = 1): Tile? {
    return Vars.world.tile(x.toInt(), y.toInt() - distance)
}
@JvmOverloads
fun Tile.up(distance: Int = 1): Tile? {
    return Vars.world.tile(x.toInt(), y.toInt() + distance)
}

//X Bottom
val Building.bottomLeftX: Int
    get() = tile.x - (block.size - 1) / 2
val Building.bottomRightX: Int
    get() = bottomLeftX + block.size - 1
// X Left
val Building.topLeftX: Int
    get() = bottomLeftX
val Building.topRightX: Int
    get() = bottomRightX

// Y Bottom
val Building.bottomLeftY: Int
    get() = tile.y - (block.size - 1) / 2
val Building.bottomRightY: Int
    get() = bottomLeftY
// Y Top
val Building.topLeftY: Int
    get() = bottomLeftY + block.size - 1
val Building.topRightY: Int
    get() = topLeftY