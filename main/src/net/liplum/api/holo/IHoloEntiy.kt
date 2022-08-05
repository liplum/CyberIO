package net.liplum.api.holo

import mindustry.game.EventType
import mindustry.gen.Healthc
import mindustry.world.Block
import mindustry.world.meta.StatUnit
import net.liplum.annotations.SubscribeEvent
import net.liplum.registry.CioStats
import plumy.core.arc.Tick
import plumy.core.math.Percentage

interface IHoloEntity {
    fun killThoroughly()
    val minHealthProportion: Float
    var restRestore: Float

    companion object {
        @JvmStatic
        @SubscribeEvent(EventType.BlockBuildEndEvent::class)
        fun registerHoloEntityInitHealth(e: EventType.BlockBuildEndEvent) {
            val he = e.tile.build
            if (he is IHoloEntity) {
                he.restRestore = he.maxHealth
                he.health = he.maxHealth * he.minHealthProportion * 0.9f
            }
        }

        val <T> T.minHealth: Float where T : Healthc, T : IHoloEntity
            get() = maxHealth() * minHealthProportion

        fun <T> T.addHoloChargeTimeStats(charge: Tick) where T : Block {
            stats.add(CioStats.holoCharge, charge, StatUnit.seconds)
        }

        fun <T> T.addHoloHpAtLeastStats(atLeast: Percentage) where T : Block {
            stats.add(CioStats.holoHpAtLeast, "$atLeast%")
        }
    }
}
