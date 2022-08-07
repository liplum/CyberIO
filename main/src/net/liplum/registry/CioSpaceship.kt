package net.liplum.registry

import mindustry.content.Liquids
import mindustry.gen.Sounds
import mindustry.type.Category
import mindustry.world.blocks.defense.turrets.ContinuousLiquidTurret
import mindustry.world.meta.BuildVisibility
import net.liplum.DebugOnly
import net.liplum.R
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
            cuttex = ContinuousLiquidTurret("cuttex").apply {
                category = Category.turret
                buildVisibility = BuildVisibility.shown
                size = 3
                shootSound = Sounds.none
                shootY = 2.8f
                addAmmo(Liquids.water, ArcFieldBulletType().apply {
                    hitColor = R.C.CuttexCyan
                    damage = 20f
                    length = 150f
                })
            }
        }
    }
}