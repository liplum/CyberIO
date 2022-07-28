package net.liplum.heimdall

typealias MdtPlanet = mindustry.type.Planet
typealias MdtSector = mindustry.type.Sector

class Planet : Iterable<Sector> {
    var localizedName = ""
    var sectors = emptyList<Sector>()
    override fun iterator() = sectors.iterator()
    override fun toString() = localizedName

    companion object {
        val X = Planet()
        fun by(p: MdtPlanet, metaMapping: IResourceMetaMapping) = Planet().apply planet@{
            localizedName = p.localizedName ?: "???"
            sectors = (p.sectors as Iterable<MdtSector>).filter {
                it.preset != null
            }.map {
                Sector.by(it, metaMapping).apply {
                    planet = this@planet
                }
            }
        }
    }
}

class Sector : Iterable<Resource> {
    var localizedName = ""
    var planet = Planet.X
    var resources = emptyList<Resource>()
    override fun iterator() = resources.iterator()
    override fun toString() = localizedName

    companion object {
        val X = Sector()
        fun by(s: MdtSector, metaMapping: IResourceMetaMapping) = Sector().apply {
            val preset = s.preset
            localizedName = if (preset != null) preset.localizedName else s.name()
            resources = s.info.resources.mapNotNull {
                val meta = metaMapping[it]
                if (meta != null)
                    Resource().apply {
                        this.meta = meta
                    }
                else null
            }
        }
    }
}