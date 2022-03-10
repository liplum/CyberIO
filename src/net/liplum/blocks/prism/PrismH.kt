package net.liplum.blocks.prism

import mindustry.gen.Bullet

enum class PrismData {
    Duplicate
}

val Any?.isDuplicate: Boolean
    get() = this == PrismData.Duplicate

fun Bullet.setDuplicate() {
    when (this.type) {
        else -> this.data = PrismData.Duplicate
    }
}