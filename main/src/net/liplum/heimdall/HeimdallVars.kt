package net.liplum.heimdall

import mindustry.content.Planets
import mindustry.ctype.UnlockableContent

object HeimdallVars {
    var resourceMetas: MutableMap<UnlockableContent, ResourceMeta> = HashMap()
    var planets: MutableList<Planet> = ArrayList()
    var mapping = object : IResourceMetaMapping {
        override fun get(context: UnlockableContent): ResourceMeta? =
            resourceMetas[context]
    }
    @JvmStatic
    fun load() {
        HeimdallLoader.loadMeta(resourceMetas)
        HeimdallLoader.generatePlanets(
            mapping,
            planets,
            listOf(
                Planets.serpulo
            )
        )
    }
}