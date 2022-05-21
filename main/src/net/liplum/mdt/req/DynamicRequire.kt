package net.liplum.mdt.req

import mindustry.Vars
import mindustry.core.GameState.State
import mindustry.game.EventType.StateChangeEvent
import mindustry.maps.Map
import mindustry.type.Sector

object DynamicRequire {
    @JvmField val registry: MutableList<DynamicRequireEntry> = ArrayList()
    //@SubscribeEvent(EventType.WorldLoadEvent::class, Only.debug)
    fun updateReq() {
        val state = Vars.state
        val map = state.map
        val sector = state.rules.sector
        val context = MapContext(map, sector)
        for (entry in registry) {
            entry.onMapChanging(context)
        }
    }
    //@SubscribeEvent(StateChangeEvent::class, Only.debug)
    fun restoreReq(e: StateChangeEvent) {
        val to = e.to
        if (to == State.menu) {
            for (entry in registry) {
                entry.onRestore()
            }
        }
    }
}

class MapContext(
    val map: Map,
    val sector: Sector?
)

interface DynamicRequireEntry {
    fun onMapChanging(context: MapContext)
    fun onRestore()
}