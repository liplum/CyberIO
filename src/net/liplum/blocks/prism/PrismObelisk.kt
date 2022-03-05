package net.liplum.blocks.prism

import mindustry.gen.Building
import mindustry.world.Block
import net.liplum.blocks.prism.Prism.PrismBuild

class PrismObelisk(name: String) : Block(name) {
    @JvmField var prismType: Prism? = null

    inner class ObeliskBuild : Building() {
        var linked: PrismBuild? = null
        override fun onProximityUpdate() {
            super.onProximityUpdate()
            if (linked == null) {
                for (b in proximity) {
                    if (b is PrismBuild && b == prismType) {

                    }
                }
            }
        }
    }
}
