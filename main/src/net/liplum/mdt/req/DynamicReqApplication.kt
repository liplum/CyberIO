package net.liplum.mdt.req

import mindustry.type.Planet

typealias DynamicReqApplier = () -> Unit

class DynamicReqApplication : DynamicRequireEntry {
    var backToDefault: DynamicReqApplier = {}
    var map: MutableMap<DynamicReqPredicate, DynamicReqApplier> = HashMap()
    override fun onMapChanging(context: MapContext) {
        val matched = map.firstNotNullOfOrNull {
            if (it.key.accept(context)) it.value
            else null
        }
        if (matched != null) {
            matched()
        }
    }

    override fun onRestore() {
        backToDefault()
    }

    fun default(setter: DynamicReqApplier): DynamicReqApplication {
        backToDefault = setter
        return this
    }
}

interface DynamicReqPredicate {
    fun accept(context: MapContext): Boolean
}

class PlanetCond(
    val planet: Planet
) : DynamicReqPredicate {
    override fun accept(context: MapContext): Boolean {
        return context.sector?.plane == planet
    }
}

class FuzzyMatchPlanetCond(
    val name: String
) : DynamicReqPredicate {
    override fun accept(context: MapContext): Boolean {
        return context.sector?.planet?.name?.contains(name) ?: false
    }
}

class DuckTypePlanetCond(
    val planet: Planet
) : DynamicReqPredicate {
    override fun accept(context: MapContext): Boolean {
        return context.sector?.planet?.hiddenItems?.containsAll(planet.hiddenItems) ?: false
    }
}