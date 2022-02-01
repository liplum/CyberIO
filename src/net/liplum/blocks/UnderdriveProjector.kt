package net.liplum.blocks

import mindustry.world.blocks.defense.OverdriveProjector

open class UnderdriveProjector(name: String) : OverdriveProjector(name) {
    inner class UnderdriveBuild : OverdriveBuild() {
        override fun realBoost(): Float {
            return if (consValid()) speedBoost * (1 - efficiency()) else 0f;
        }
    }
}