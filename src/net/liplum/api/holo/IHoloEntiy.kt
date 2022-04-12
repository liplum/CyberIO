package net.liplum.api.holo

import arc.Events
import mindustry.game.EventType

interface IHoloEntity {
    fun killThoroughly()
    val minHealthProportion: Float

    companion object {
        @JvmStatic
        fun registerHoloEntityInitHealth() {
            Events.on(EventType.BlockBuildEndEvent::class.java) {
                val he = it.tile.build
                if (he is IHoloEntity) {
                    he.health = he.maxHealth * he.minHealthProportion * 0.9f
                }
            }
        }
    }
}