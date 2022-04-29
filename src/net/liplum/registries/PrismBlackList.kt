package net.liplum.registries

import mindustry.content.UnitTypes
import net.liplum.blocks.prism.banInWeapon
import net.liplum.blocks.prism.banInWeapons

object PrismBlackList {
    //TODO: ban progressed.entities.bullet.physical.MagnetBulletType
    @JvmStatic
    fun load() {
        UnitTypes.arkyid.banInWeapon("spiroct-weapon")
        UnitTypes.spiroct.banInWeapons(
            "spiroct-weapon",
            "mount-purple-weapon"
        )
    }
}

