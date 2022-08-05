package net.liplum.heimdall

import arc.math.Rand
import mindustry.ctype.ContentType
import mindustry.ctype.UnlockableContent
import plumy.core.Out

object HeimdallLoader {
    @JvmStatic
    fun loadMeta(@Out metas: MutableMap<UnlockableContent, ResourceMeta>) {
        for ((content, id) in ResourceMapping) {
            val meta = ResourceMeta().apply {
                this.id = id
                icon = content.fullIcon
            }
            if (content.contentType == ContentType.item)
                meta.category = ResourceCategory.Mineral
            else if (content.contentType == ContentType.liquid)
                meta.category = ResourceCategory.Liquid
            else continue
            metas[content] = meta
        }
    }

    var resourceID: ResourceID = 0
    @JvmStatic
    fun generatePlanets(
        metaMapping: IResourceMetaMapping,
        @Out planets: MutableList<Planet>,
        mdtPlanets: List<MdtPlanet>,
    ) {
        val rand = Rand()
        val context = object : IResourceGeneratingContext {
            override fun genID() = resourceID++
            override fun randReserve(): ResourceReserve {
                val values = ResourceReserve.values()
                return values[rand.random(0, values.size - 1)]
            }

            override fun randTimeReq(
                meta: ResourceMeta,
                reserve: ResourceReserve,
            ): Int {
                return reserve.times * 120
            }
        }
        for (planet in mdtPlanets) {
            planets.add(genPlanet(planet, context, metaMapping))
        }
    }
    @JvmStatic
    fun genPlanet(
        mdtPlanet: MdtPlanet,
        context: IResourceGeneratingContext,
        metaMapping: IResourceMetaMapping,
    ): Planet {
        val planet = Planet.by(mdtPlanet, metaMapping)
        for (sector in planet) for (resource in sector) {
            resource.apply {
                id = context.genID()
                reserve = context.randReserve()
                timeReq = context.randTimeReq(meta, reserve)
            }
        }
        return planet
    }
}
