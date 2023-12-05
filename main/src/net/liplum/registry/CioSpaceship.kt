package net.liplum.registry

import arc.func.Prov
import mindustry.content.Items
import mindustry.content.Liquids
import mindustry.entities.part.DrawPart.PartProgress
import mindustry.gen.Sounds
import mindustry.type.Category
import mindustry.world.blocks.defense.turrets.ContinuousLiquidTurret
import mindustry.world.blocks.defense.turrets.PowerTurret
import mindustry.world.meta.BuildVisibility
import net.liplum.*
import net.liplum.annotations.DependOn
import net.liplum.bullet.ArcFieldBulletType
import net.liplum.bullet.ArcWaveBulletType
import plumy.dsl.addAmmo
import plumy.dsl.drawTurret
import plumy.dsl.plus
import plumy.dsl.regionPart

object CioSpaceship {
    @JvmStatic
    lateinit var cutteX: ContinuousLiquidTurret
    @JvmStatic
    lateinit var discharger: PowerTurret
    @DependOn(
        "CioItem.ic",
        "CioFluid.cyberion"
    )
    fun cutteX() {
        cutteX = ContinuousLiquidTurret("cuttex").apply {
            category = Category.turret
            buildVisibility = BuildVisibility.shown
            requirements = arrayOf(
            )
            size = 3
            shootSound = Sounds.none
            shootY = 2.8f
            rotateSpeed = 8f
            shootWarmupSpeed = 0.05f
            buildType = Prov { ContinuousLiquidTurretBuild() }
            VanillaSpec {
                scaledHealth = 250f
                requirements = arrayOf(
                    Items.titanium + 200,
                    Items.silicon + 80,
                    Items.thorium + 50,
                )
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
                    hitColor = Var.Hologram
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
            ErekirSpec {
                scaledHealth = 300f
                requirements = arrayOf(
                    Items.tungsten + 120,
                    Items.thorium + 80,
                    Items.carbide + 30,
                )
                range = 165f
                addAmmo(Liquids.water, ArcFieldBulletType {
                    hitColor = R.C.CuttexCyan
                    damage = 40f
                    length = 110f
                })
                addAmmo(Liquids.cryofluid, ArcFieldBulletType {
                    hitColor = R.C.CuttexCyan
                    damage = 80f
                    length = 120f
                })
                addAmmo(CioFluid.cyberion, ArcFieldBulletType {
                    hitColor = Var.Hologram
                    damage = 150f
                    length = 145f
                    lightenIntensity = 0.2f
                })
                addAmmo(Liquids.cyanogen, ArcFieldBulletType {
                    hitColor = R.C.CuttexCyan
                    damage = 200f
                    length = 165f
                })
            }
            drawTurret {
                regionPart("-side") {
                    mirror = true
                    under = true
                    progress = PartProgress.warmup
                    moveY = -7f
                }
            }
        }
    }
    @DependOn(
        "CioItem.ic",
    )
    fun discharger() {
        DebugOnly {
            discharger = PowerTurret("discharger").apply {
                category = Category.turret
                buildVisibility = BuildVisibility.shown
                requirements = arrayOf(
                )
                size = 3
                consumePower(7.5f)
                shootType = ArcWaveBulletType {
                    lifetime = 180f
                    trailColor = R.C.BrainWave
                }
            }
        }
    }
}