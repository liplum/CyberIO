package net.liplum.utils

import arc.math.geom.Point2
import mindustry.Vars
import mindustry.content.Blocks
import mindustry.ctype.Content
import mindustry.ctype.UnlockableContent
import mindustry.entities.Units
import mindustry.game.Team
import mindustry.gen.Building
import mindustry.type.UnitType
import mindustry.world.Block
import mindustry.world.Tile
import mindustry.world.blocks.payloads.BuildPayload
import mindustry.world.blocks.payloads.PayloadConveyor
import mindustry.world.blocks.payloads.UnitPayload
import plumy.core.Out
import plumy.core.math.Point2f
import plumy.dsl.*

/**
 * Get the world coordinate of building
 * @param pos Output Parameter.
 * @return [pos]
 */
fun Building.worldPos(@Out pos: Point2f): Point2f {
    pos.set(x, y)
    return pos
}

inline fun <reified T> Int.inPayload(): T? {
    val build = this.build
    if (build is PayloadConveyor.PayloadConveyorBuild) {
        val payload = build.payload
        if (payload is BuildPayload) {
            return payload.build as? T
        } else if (payload is UnitPayload) {
            return payload.unit as? T
        }
    }
    return null
}

inline fun <reified T> Point2.inPayload(): T? {
    val build = this.build
    if (build is PayloadConveyor.PayloadConveyorBuild) {
        val payload = build.payload
        if (payload is BuildPayload) {
            return payload.build as? T
        } else if (payload is UnitPayload) {
            return payload.unit as? T
        }
    }
    return null
}

inline fun <reified T> Int.inPayloadBuilding(): T? where T : Building {
    val build = this.build
    if (build is PayloadConveyor.PayloadConveyorBuild) {
        return (build.payload as? BuildPayload)?.build as? T
    }
    return null
}

inline fun <reified T> Int.inPayloadUnit(): T? where T : MUnit {
    val build = this.build
    if (build is PayloadConveyor.PayloadConveyorBuild) {
        return (build.payload as? UnitPayload)?.unit as? T
    }
    return null
}

val Building?.inPayload: Boolean
    get() = this != null && this.tile == Vars.emptyTile
val Building?.existsOrInPayload: Boolean
    get() = this != null && (this.tile.build == this || this.tile == Vars.emptyTile)

fun ItemTypeAmount(): Int =
    Vars.content.items().size

fun LiquidTypeAmount(): Int =
    Vars.content.liquids().size

inline fun ForProximity(centerTileX: TileXY, centerTileY: TileXY, tileDistance: TileXY, func: (Tile) -> Unit) {
    val minX = centerTileX - tileDistance
    val minY = centerTileY - tileDistance
    val maxX = centerTileX + tileDistance
    val maxY = centerTileY + tileDistance
    val world = Vars.world
    for (i in minX..maxX) {
        for (j in minY..maxY) {
            val tile = world.tile(i, j)
            if (tile != null) {
                func(tile)
            }
        }
    }
}

inline fun Tile.ForProximity(tileDistance: TileXY, func: (Tile) -> Unit) {
    val minX = x - tileDistance
    val minY = y - tileDistance
    val maxX = x + tileDistance
    val maxY = y + tileDistance
    val world = Vars.world
    for (i in minX..maxX) {
        for (j in minY..maxY) {
            val tile = world.tile(i, j)
            if (tile != null) {
                func(tile)
            }
        }
    }
}

inline fun Building.ForProximity(tileDistance: TileXY, func: (Tile) -> Unit) {
    ForProximity(this.tileX(), this.tileY(), tileDistance, func)
}

inline fun Building.ForProximity(func: (Tile) -> Unit) {
    ForProximity(this.tileX(), this.tileY(), 1, func)
}

fun UnitType.pctOfTeamOwns(team: Team) =
    team.data().countType(this).toFloat() / Units.getCap(team)

inline fun ForEachContent(func: (Content) -> Unit) {
    Vars.content.contentMap.flatMap { it.toList() }.forEach {
        func(it)
    }
}

inline fun ForEachUnlockableContent(func: (UnlockableContent) -> Unit) {
    Vars.content.contentMap.flatMap { it.toList() }.forEach {
        if (it is UnlockableContent) func(it)
    }
}
@JvmOverloads
fun Tile.left(distance: TileXY = 1): Tile? {
    return Vars.world.tile(x.toInt() - distance, y.toInt())
}
@JvmOverloads
fun Tile.right(distance: TileXY = 1): Tile? {
    return Vars.world.tile(x.toInt() + distance, y.toInt())
}
@JvmOverloads
fun Tile.bottom(distance: TileXY = 1): Tile? {
    return Vars.world.tile(x.toInt(), y.toInt() - distance)
}
@JvmOverloads
fun Tile.up(distance: TileXY = 1): Tile? {
    return Vars.world.tile(x.toInt(), y.toInt() + distance)
}
//X Bottom
val Building.bottomLeftX: TileXY
    get() = tile.x - (block.size - 1) / 2
val Building.bottomRightX: TileXY
    get() = bottomLeftX + block.size - 1
// X Left
val Building.topLeftX: TileXY
    get() = bottomLeftX
val Building.topRightX: TileXY
    get() = bottomRightX
// Y Bottom
val Building.bottomLeftY: TileXY
    get() = tile.y - (block.size - 1) / 2
val Building.bottomRightY: TileXY
    get() = bottomLeftY
// Y Top
val Building.topLeftY: TileXY
    get() = bottomLeftY + block.size - 1
val Building.topRightY: TileXY
    get() = topLeftY

fun Building.isDiagonalTo(other: Block, x: TileXY, y: TileXY) =
    plumy.core.math.isDiagonalTo(
        other.getCenterWorldXY(x), other.getCenterWorldXY(y),
        this.x, this.y,
    )

fun Building.tileEquals(pos: PackedPos): Boolean =
    pos() == pos

fun Building.tilePoint(): Point2 =
    Point2(tileX(), tileY())

fun Building.tileEquals(pos: Point2?): Boolean =
    pos != null && tileX() == pos.x && tileY() == pos.y

fun Point2?.packSafe(): Int =
    this?.pack() ?: -1

enum class TileOreType {
    None, Ground, Wall
}

fun Tile.getOreType(): TileOreType {
    val block = block()
    return if (block == Blocks.air) {
        // Try to check if this is a ground ore
        val overlay = overlay()
        if (overlay.itemDrop != null) TileOreType.Ground
        else TileOreType.None
    } else {
        // Try to check if this is a wall ore
        if (block.itemDrop != null) TileOreType.Wall
        else TileOreType.None
    }
}

fun worldWidth(): Float =
    Vars.world.tiles.width * Vars.tilesize + Vars.finalWorldBounds

fun worldHeight(): Float =
    Vars.world.tiles.height * Vars.tilesize + Vars.finalWorldBounds
