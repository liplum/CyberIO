@file:Suppress("GrazieInspection")

package net.liplum.registries

import mindustry.content.UnitTypes
import mindustry.gen.Bullet
import net.liplum.blocks.prism.PrismData
import net.liplum.blocks.prism.banInWeapon
import net.liplum.blocks.prism.banInWeapons
import net.liplum.blocks.prism.banNameInPrism
import kotlin.math.roundToInt

object PrismBlackList {
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
        /**
         * Ban this in Progression Ministry, although it changed its name.
         * Name used before : Progressed Materials
         * Because it casts the [Bullet.data] to its `CritBulletData` without type checking.
         */
        "progressed.entities.bullet.physical.CritBulletType".banNameInPrism()
        /**
         * Ban this in Progression Ministry.
         * Because it will copy too much.
         */
        "progressed.entities.bullet.energy.RiftBulletType".banNameInPrism()
        /**
         * Ban this in Progression Ministry.
         * Because it casts the [Bullet.data] to its `CrossLaserData` without type checking.
         */
        "progressed.entities.bullet.energy.CrossLaserBulletType".banNameInPrism()
        /**
         * Ban this in Project Unity
         * Because it casts the data as Array<Color> without checking type.
         *  for(Color color : (Color[])b.data){
         */
        "unity.entities.bullet.exp.ExpLaserBlastBulletType".banNameInPrism()
    }
}

