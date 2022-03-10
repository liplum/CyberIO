@file:Suppress("UNCHECKED_CAST")

package net.liplum.utils

import arc.math.Mathf
import mindustry.Vars
import mindustry.gen.Building

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
