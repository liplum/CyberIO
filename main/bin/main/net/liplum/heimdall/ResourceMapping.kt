package net.liplum.heimdall

import mindustry.content.Items.*
import mindustry.content.Liquids.*
import mindustry.ctype.UnlockableContent

object ResourceMapping :
    Iterable<Map.Entry<UnlockableContent, Int>> {
    var id = 0
        private set
    val mapping by lazy {
        LinkedHashMap(
            arrayOf<UnlockableContent>(
                scrap, copper, lead, graphite, coal,
                titanium, thorium, plastanium, sand,
                water, slag, oil
            ).convert()
        )
    }

    private fun Array<UnlockableContent>.convert():
            Map<UnlockableContent, Int> =
        this.associateWith { genID() }

    fun genID(): Int = id++
    operator fun get(content: UnlockableContent): Int? =
        mapping[content]

    override fun iterator() = mapping.iterator()
}