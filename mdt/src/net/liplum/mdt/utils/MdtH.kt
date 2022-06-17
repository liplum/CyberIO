@file:JvmName("MdtH")
@file:Suppress("UNCHECKED_CAST")

package net.liplum.mdt.utils

import arc.math.Mathf
import arc.math.geom.Point2
import arc.math.geom.Position
import mindustry.Vars
import mindustry.content.Blocks
import mindustry.ctype.Content
import mindustry.ctype.UnlockableContent
import mindustry.entities.Units
import mindustry.entities.bullet.BasicBulletType
import mindustry.entities.bullet.BulletType
import mindustry.entities.pattern.ShootPattern
import mindustry.game.Team
import mindustry.gen.Building
import mindustry.type.UnitType
import mindustry.world.Block
import mindustry.world.Tile
import mindustry.world.blocks.payloads.BuildPayload
import mindustry.world.blocks.payloads.PayloadConveyor
import mindustry.world.blocks.payloads.PayloadSource
import mindustry.world.blocks.payloads.UnitPayload
import net.liplum.lib.Out
import net.liplum.lib.math.Point2f

typealias TileXY = Int
typealias TileXYs = Short
typealias TileXYf = Float
typealias TileXYd = Double
typealias WorldXY = Float
typealias PackedPos = Int
typealias Pos = Point2

fun PackedPos.unpack(): Pos =
    Point2.unpack(this)

val Pos.isEmpty: Boolean
    get() = x < 0 || y < 0

fun NewEmptyPos() = Pos(-1, -1)
fun tileAt(x: TileXY, y: TileXY): Tile? =
    Vars.world.tile(x, y)

fun tileAt(x: TileXYf, y: TileXYf): Tile? =
    Vars.world.tile(x.toInt(), y.toInt())

fun tileAt(x: TileXYd, y: TileXYd): Tile? =
    Vars.world.tile(x.toInt(), y.toInt())

fun buildAt(x: TileXY, y: TileXY): Building? =
    Vars.world.build(x, y)

fun buildAt(x: TileXYf, y: TileXYf): Building? =
    Vars.world.build(x.toInt(), y.toInt())

fun buildAt(x: TileXYd, y: TileXYd): Building? =
    Vars.world.build(x.toInt(), y.toInt())

fun Tile.dstWorld(x: TileXY, y: TileXY): WorldXY =
    this.dst(x * Vars.tilesize.toFloat(), y * Vars.tilesize.toFloat())
/**
 * Get the world coordinate of building
 * @param pos Output Parameter.
 * @return [pos]
 */
fun Building.worldPos(@Out pos: Point2f): Point2f {
    pos.set(x, y)
    return pos
}

val Int.build: Building?
    get() = Vars.world.build(this)
val Point2.build: Building?
    get() = Vars.world.build(x, y)

fun <T : Building> Int.TE(): T? =
    Vars.world.build(this) as? T

fun <T> Int.TEAny(): T? =
    Vars.world.build(this) as? T

fun <T> Int.inPayload(): T? {
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

fun <T> Point2.inPayload(): T? {
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

fun <T> Int.inPayloadBuilding(): T? where T : Building {
    val build = this.build
    if (build is PayloadConveyor.PayloadConveyorBuild) {
        return (build.payload as? BuildPayload)?.build as? T
    }
    return null
}

fun <T> Int.inPayloadUnit(): T? where T : MdtUnit {
    val build = this.build
    if (build is PayloadConveyor.PayloadConveyorBuild) {
        return (build.payload as? UnitPayload)?.unit as? T
    }
    return null
}

val Building?.exists: Boolean
    get() = this != null && this.tile.build == this
val Building?.inPayload: Boolean
    get() = this != null && this.tile == Vars.emptyTile
val Building?.existsOrInPayload: Boolean
    get() = this != null && (this.tile.build == this || this.tile == Vars.emptyTile)

fun <T> Array<T>.randomOne(): T =
    this[Mathf.random(0, this.size - 1)]

val Content.ID: Int
    get() = this.id.toInt()

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

fun <T> Class<T>.registerPayloadSource() where T : UnitType {
    val source = Blocks.payloadSource as PayloadSource
    source.config(this) { build: PayloadSource.PayloadSourceBuild, unitType ->
        if (source.canProduce(unitType) && build.unit != unitType) {
            build.unit = unitType
            build.block = null
            build.payload = null
            build.scl = 0f
        }
    }
}

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

fun BasicBulletType(textureName: String): BasicBulletType {
    return BasicBulletType(1f, 1f, textureName)
}

fun <T : BulletType> BulletType.copyAs(): T =
    this.copy() as T

fun <T : ShootPattern> ShootPattern.copyAs(): T =
    this.copy() as T
/**
 * Tile xy to world xy. Take block's offset into account
 */
fun Block.toCenterWorldXY(xy: TileXYs): WorldXY =
    offset + xy * Vars.tilesize
/**
 * Tile xy to world xy. Take block's offset into account
 */
fun Block.toCenterWorldXY(xy: TileXY): WorldXY =
    offset + xy * Vars.tilesize

fun Block.toCenterTileXY(xy: TileXY): TileXYf =
    offset + xy

val WorldXY.tileXY: TileXY
    get() = (this / Vars.tilesize).toInt()
/**
 * Tile xy to world xy
 */
val TileXYs.worldXY: WorldXY
    get() = this.toFloat() * Vars.tilesize
/**
 * Tile xy to world xy
 */
val TileXY.worldXY: WorldXY
    get() = this.toFloat() * Vars.tilesize
val TileXYf.worldXY: WorldXY
    get() = this * Vars.tilesize
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
    net.liplum.lib.math.isDiagonalTo(
        other.toCenterWorldXY(x), other.toCenterWorldXY(y),
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

fun Position.inWorld(): Boolean {
    if (x < -Vars.finalWorldBounds ||
        y < -Vars.finalWorldBounds
    ) return false
    if (x > Vars.world.tiles.height * Vars.tilesize + Vars.finalWorldBounds * 2 ||
        y > Vars.world.tiles.height * Vars.tilesize + Vars.finalWorldBounds
    ) return false
    return true
}