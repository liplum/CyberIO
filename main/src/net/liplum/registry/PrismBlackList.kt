@file:Suppress("GrazieInspection")

package net.liplum.registry

import mindustry.content.UnitTypes
import mindustry.gen.Bullet
import net.liplum.annotations.SubscribeEvent
import net.liplum.api.prism.PrismBlackList.banInWeapon
import net.liplum.api.prism.PrismBlackList.banInWeapons
import net.liplum.api.prism.PrismBlackList.banNameInPrism
import net.liplum.api.prism.PrismData
import net.liplum.event.CioLoadContentEvent

@Suppress("SpellCheckingInspection")
object PrismBlackList {
    @JvmStatic
    @SubscribeEvent(CioLoadContentEvent::class)
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

