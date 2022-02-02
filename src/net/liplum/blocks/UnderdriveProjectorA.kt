package net.liplum.blocks

import mindustry.Vars
import mindustry.gen.Building
import mindustry.world.blocks.defense.OverdriveProjector

open class UnderdriveProjectorA(name: String) : OverdriveProjector(name) {
    var slowDown: Float = 0.2f

    companion object {
        @JvmStatic
        fun applyBuildSlow(build: Building, laxity: Float, duration: Float) {
            val oldTS = build.timeScale
            val oldTSD = build.timeScaleDuration
            build.timeScale = oldTS.coerceAtMost(laxity)
            build.timeScaleDuration = oldTSD.coerceAtLeast(duration)
        }
    }

    open inner class UnderdriveBuild : OverdriveBuild() {
        override fun realBoost(): Float {
            //return if (consValid()) speedBoost * (1 - efficiency()) else 0f;
            return slowDown
        }

        override fun updateTile() {
            Vars.indexer.eachBlock(this, range,
                {
                    true
                }) {
                applyBuildSlow(it, realBoost(), 60f)
            }
        }
    }
}