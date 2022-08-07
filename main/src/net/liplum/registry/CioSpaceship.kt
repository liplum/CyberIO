package net.liplum.registry

import mindustry.content.Liquids
import mindustry.gen.Sounds
import mindustry.type.Category
import mindustry.world.blocks.defense.turrets.ContinuousLiquidTurret
import mindustry.world.meta.BuildVisibility
import net.liplum.DebugOnly
import net.liplum.R
import net.liplum.S
import net.liplum.annotations.DependOn
import net.liplum.bullet.ArcFieldBulletType
import net.liplum.mdt.utils.addAmmo

object CioSpaceship {
    @JvmStatic lateinit var cuttex: ContinuousLiquidTurret
    @DependOn(
        "CioItem.ic",
        "CioFluid.cyberion"
    )
    fun cuttex() {
        DebugOnly {
            cuttex = ContinuousLiquidTurret("cuttex").apply turret@{
                category = Category.turret
                buildVisibility = BuildVisibility.shown
                size = 3
                shootSound = Sounds.none
                shootY = 2.8f
                rotateSpeed = 8f
                range = 160f
                addAmmo(Liquids.water, ArcFieldBulletType {
                    hitColor = R.C.CuttexCyan
                    damage = 12f
                    length = 100f
                })
                addAmmo(Liquids.cryofluid, ArcFieldBulletType {
                    hitColor = R.C.CuttexCyan
                    damage = 40f
                    length = 130f
                })
                addAmmo(CioFluid.cyberion, ArcFieldBulletType {
                    hitColor = S.Hologram
                    damage = 140f
                    length = 145f
                    lightenIntensity = 0.2f
                })
                addAmmo(Liquids.cyanogen, ArcFieldBulletType {
                    hitColor = R.C.CuttexCyan
                    damage = 180f
                    length = 160f
                })
            }
        }
    }
}