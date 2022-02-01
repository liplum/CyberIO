package net.liplum.blocks

import mindustry.world.Block
import net.liplum.api.ILoadableContent

open class AnimedBlock(name: String) : Block(name), ILoadableContent {
    var loadListener: HashSet<() -> Unit> = HashSet()
    override fun addLoadListener(listener: () -> Unit) {
        loadListener.add(listener)
    }

    override fun load() {
        super.load()
        for (li in loadListener) {
            li()
        }
    }
}