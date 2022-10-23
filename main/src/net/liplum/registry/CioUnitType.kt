package net.liplum.registry

import mindustry.entities.bullet.BasicBulletType
import mindustry.type.Weapon
import mindustry.type.ammo.PowerAmmoType
import net.liplum.DebugOnly
import net.liplum.R
import net.liplum.annotations.DependOn
import net.liplum.cio
import net.liplum.flesh.BrainUnitType
import net.liplum.utils.registerUnitType

object CioUnitType {
    @JvmStatic lateinit var brain: BrainUnitType
    @DependOn("CioItem.ic")
    fun brain() {
        DebugOnly {
            registerUnitType(R.Unit.Brain)
            brain = BrainUnitType(R.Unit.Brain).apply {
                flying = true
                drag = 0.06f
                accel = 0.12f
                speed = 1.5f
                health = 100f
                engineSize = 1.8f
                engineOffset = 5.7f
                range = 50f

                ammoType = PowerAmmoType(500f)
                weapons.add(Weapon("${R.Unit.Brain}-hand".cio).apply {
                    x = 8f
                    y = 8f
                    recoil = -10f
                    reload = 7f
                    bullet = BasicBulletType(10f, 1f).apply {
                        recoil = -0.7f
                    }
                })
            }
        }
    }
}