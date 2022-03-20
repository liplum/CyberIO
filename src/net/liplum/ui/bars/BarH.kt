package net.liplum.ui.bars

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
    try {
        this.remove("items")
    } catch (_: RuntimeException) {
    }
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
