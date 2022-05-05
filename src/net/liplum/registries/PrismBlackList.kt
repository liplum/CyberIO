package net.liplum.registries

import mindustry.content.UnitTypes
import net.liplum.blocks.prism.PrismData
import net.liplum.blocks.prism.banInWeapon
import net.liplum.blocks.prism.banInWeapons
import net.liplum.blocks.prism.banNameInPrism

object PrismBlackList {
    //TODO: ban progressed.entities.bullet.physical.MagnetBulletType
    @JvmStatic
    fun load() {
        UnitTypes.arkyid.banInWeapon("spiroct-weapon")
        UnitTypes.spiroct.banInWeapons(
            "spiroct-weapon",
            "mount-purple-weapon"
        )
        /**
         * Ban this in Progressed Materials
         * because it will update self's data each tick overwriting [PrismData.Duplicate]
         */
        "progressed.entities.bullet.physical.MagnetBulletType".banNameInPrism()
    }
}

