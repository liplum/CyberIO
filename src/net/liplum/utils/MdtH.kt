@file:Suppress("UNCHECKED_CAST")

package net.liplum.utils

import arc.math.Mathf
import mindustry.Vars
import mindustry.ctype.Content
import mindustry.entities.Units
import mindustry.game.Team
import mindustry.gen.Building
import mindustry.type.UnitType
import mindustry.world.Tile

val Int.build: Building?
    get() = Vars.world.build(this)

fun <T : Building> Int.te(): T? =
    Vars.world.build(this) as? T

fun <T> Int.tea(): T? =
    Vars.world.build(this) as? T

val Building?.exists: Boolean
    get() = this != null && this.tile.build == this

fun <T> Array<T>.randomOne(): T =
    this[Mathf.random(0, this.size - 1)]

val Content.ID: Int
    get() = this.id.toInt()

fun ItemTypeAmount(): Int =
    Vars.content.items().size

fun LiquidTypeAmount(): Int =
    Vars.content.liquids().size

inline fun ForProximity(centerX: Int, centerY: Int, tileDistance: Int, func: (Tile) -> Unit) {
    val minX = centerX - tileDistance
    val minY = centerY - tileDistance
    val maxX = centerX + tileDistance
    val maxY = centerY + tileDistance
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

inline fun Building.ForProximity(tileDistance: Int, func: (Tile) -> Unit) {
    ForProximity(this.tileX(), this.tileY(), tileDistance, func)
}

inline fun Building.ForProximity(func: (Tile) -> Unit) {
    ForProximity(this.tileX(), this.tileY(), 1, func)
}

fun UnitType.pctOfTeamOwns(team: Team) =
    team.data().countType(this).toFloat() / Units.getCap(team)