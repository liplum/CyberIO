package net.liplum.registry

import mindustry.content.Liquids
import mindustry.gen.Sounds
import mindustry.type.Category
import mindustry.world.blocks.defense.turrets.ContinuousLiquidTurret
import mindustry.world.meta.BuildVisibility
import net.liplum.DebugOnly
import net.liplum.R
import net.liplum.annotations.DependOn
import net.liplum.bullet.FieldBulletType
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
                addAmmo(Liquids.water, FieldBulletType().apply {
                    fieldColor = R.C.CuttexCyan
                })
            }
        }
    }
}