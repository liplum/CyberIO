package net.liplum.lib.ui.bars

import arc.graphics.Color
import mindustry.gen.Building
import mindustry.ui.Bar
import mindustry.world.Block
import mindustry.world.meta.BlockBars

fun BlockBars.removeIfExist(name: String) {
    try {
        this.remove(name)
    } catch (_: RuntimeException) {
    }
}

fun BlockBars.removeItems() {
    this.removeIfExist("items")
}

fun BlockBars.removeLiquid() {
    this.removeIfExist("liquid")
}

inline fun <T : Building> Block.AddBar(
    key: String,
    crossinline name: T.() -> String,
    crossinline color: T.() -> Color,
    crossinline fraction: T.() -> Float
) {
    this.bars.add<T>(key) {
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
    crossinline fraction: Building.() -> Float
) {
    this.bars.add<Building>(key) {
        Bar(
            { it.name() },
            { it.color() },
            { it.fraction() }
        )
    }
}