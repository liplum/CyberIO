package net.liplum.lib.ui.bars

import arc.graphics.Color
import mindustry.gen.Building
import mindustry.ui.Bar
import mindustry.world.Block

fun Block.removeItemsInBar() {
    this.removeBar("items")
}

fun Block.removeLiquidInBar() {
    this.removeBar("liquid")
}

inline fun <reified T : Building> Block.AddBar(
    key: String,
    crossinline name: T.() -> String,
    crossinline color: T.() -> Color,
    crossinline fraction: T.() -> Float,
) {
    addBar<T>(key) {
        Bar(
            { it.name() },
            { it.color() },
            { it.fraction() }
        )
    }
}

inline fun Block.addBar(
    key: String,
    crossinline name: Building.() -> String,
    crossinline color: Building.() -> Color,
    crossinline fraction: Building.() -> Float,
) {
    addBar<Building>(key) {
        Bar(
            { it.name() },
            { it.color() },
            { it.fraction() }
        )
    }
}