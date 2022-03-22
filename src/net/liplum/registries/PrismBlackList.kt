package net.liplum.registries

import mindustry.content.UnitTypes
import net.liplum.blocks.prism.banInWeapon
import net.liplum.blocks.prism.banInWeapons

object PrismBlackList {
    @JvmStatic
    fun block() {
        UnitTypes.arkyid.banInWeapon("spiroct-weapon")
        UnitTypes.spiroct.banInWeapons(
            "spiroct-weapon",
            "mount-purple-weapon"
        )
    }
}

