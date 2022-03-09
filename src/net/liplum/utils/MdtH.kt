package net.liplum.utils

import mindustry.Vars
import mindustry.gen.Building

val Int.build: Building?
    get() = Vars.world.build(this)

fun <T : Building> Int.te(): T? =
    Vars.world.build(this) as? T

val Building?.exists: Boolean
    get() = this != null && this.tile.build == this