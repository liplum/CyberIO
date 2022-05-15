package net.liplum.api.holo

import arc.Events
import mindustry.game.EventType
import mindustry.gen.Healthc

interface IHoloEntity {
    fun killThoroughly()
    val minHealthProportion: Float
    var restRestore: Float

    companion object {
        @JvmStatic
        fun registerHoloEntityInitHealth() {
            Events.on(EventType.BlockBuildEndEvent::class.java) {
                val he = it.tile.build
                if (he is IHoloEntity) {
                    he.restRestore = he.maxHealth
                    he.health = he.maxHealth * he.minHealthProportion * 0.9f
                }
            }
        }

        val <T> T.minHealth: Float where T : Healthc, T : IHoloEntity
            get() = maxHealth() * minHealthProportion
    }
}