package net.liplum.registries

import arc.func.Prov
import arc.graphics.Color
import mindustry.ai.types.BuilderAI
import mindustry.ai.types.DefenderAI
import mindustry.ai.types.MinerAI
import mindustry.ai.types.RepairAI
import mindustry.content.Fx
import mindustry.content.Items
import mindustry.entities.abilities.RepairFieldAbility
import mindustry.entities.bullet.LaserBoltBulletType
import mindustry.entities.bullet.MissileBulletType
import mindustry.gen.Sounds
import mindustry.type.ammo.ItemAmmoType
import mindustry.type.ammo.PowerAmmoType
import mindustry.world.meta.BlockFlag
import net.liplum.Cio
import net.liplum.R
import net.liplum.bullets.RuvikBullet
import net.liplum.bullets.STEM_VERSION
import net.liplum.holo.*
import net.liplum.scripts.NpcUnitType
import net.liplum.utils.NewUnitType
import net.liplum.utils.registerPayloadSource

object CioUnitTypes : ContentTable {
    @JvmStatic lateinit var holoMiner: HoloUnitType
    @JvmStatic lateinit var holoFighter: HoloUnitType
    @JvmStatic lateinit var holoGuardian: HoloUnitType
    @JvmStatic lateinit var holoArchitect: HoloUnitType
    @JvmStatic lateinit var holoSupporter: HoloUnitType
    override fun firstLoad() {
        HoloUnitType::class.java.registerPayloadSource()
        NpcUnitType::class.java.registerPayloadSource()

        holoMiner = NewUnitType(R.Unit.HoloMiner, ::HoloUnitType, ::HoloUnit).apply {
            AutoLife(maxHealth = 1600f, lose = 0.08f)
            health = 2000f
            speed = 2f
            defaultController = Prov { MinerAI() }
            lowAltitude = true
            flying = true
            hovering = true
            mineSpeed = 10f
            mineTier = 5
            armor = 2f
            buildSpeed = 1f
            drag = 0.06f
            accel = 0.12f
            engineSize = 1.8f
            engineOffset = 5.7f
            range = 50f

            ammoType = PowerAmmoType(500f)
        }

        holoFighter = NewUnitType(R.Unit.HoloFighter, ::HoloUnitType, ::HoloUnit).apply {
            AutoLife(maxHealth = 2200f, lose = 0.2f)
            speed = 4f
            accel = 0.08f
            drag = 0.016f
            buildSpeed = 1f
            flying = true
            hovering = true
            hitSize = 9f
            targetAir = true
            rotateSpeed = 25f
            engineOffset = 7f
            range = 150f
            armor = 3f
            playerTargetFlags = arrayOf(null)
            targetFlags = arrayOf(BlockFlag.factory, null)
            commandLimit = 5
            circleTarget = true
            ammoType = ItemAmmoType(Items.plastanium)
            enableRuvikTip = true
            ruvikTipRange = 220f
            weapons.add(HoloWeapon("holo-fighter-gun".Cio).apply {
                top = false
                shootSound = Sounds.flame
                shootY = 2f
                reload = 11f
                recoil = 1f
                ejectEffect = Fx.none
                shootCost = 15f
                bullet = RuvikBullet(1.6f, 35f).apply {
                    stemVersion = STEM_VERSION.STEM1
                    maxRange = ruvikTipRange
                    width = 10f
                    height = 10f
                    hitSize = 10f
                    lifetime = 240f
                    frontColor = R.C.Holo
                    backColor = R.C.HoloDark
                }
            })
        }

        holoGuardian = NewUnitType(R.Unit.HoloGuardian, ::HoloUnitType, ::HoloUnit).apply {
            AutoLife(maxHealth = 5000f, lose = 0.3f)
            abilities.add(
                HoloForceField(
                    60f, 4f, 2200f, 60f * 8
                )
            )
            defaultController = Prov { DefenderAI() }
            HoloOpacity = 0.4f
            speed = 1.6f
            flying = true
            hovering = true
            buildSpeed = 2.6f
            drag = 0.05f
            accel = 0.1f
            rotateSpeed = 15f
            engineSize = 1.8f
            engineOffset = 5.7f
            hitSize = 15f
            armor = 5f
        }
        holoArchitect = NewUnitType(R.Unit.HoloArchitect, ::HoloUnitType, ::HoloUnit).apply {
            AutoLife(maxHealth = 1600f, lose = 0.15f)
            defaultController = Prov { BuilderAI() }
            speed = 3.5f
            HoloOpacity = 0.4f
            ColorOpacity = 0.05f
            flying = true
            hovering = true
            drag = 0.06f
            accel = 0.12f
            lowAltitude = true
            engineSize = 1.8f
            engineOffset = 3.7f
            hitSize = 15f
            armor = 5f
            buildSpeed = 4.6f
            ammoType = PowerAmmoType(900f)

            weapons.add(HoloWeapon().apply {
                x = 0f
                y = 5f
                top = false
                reload = 30f
                ejectEffect = Fx.none
                recoil = 2f
                shots = 2
                shootSound = Sounds.missile
                velocityRnd = 0.5f
                inaccuracy = 15f
                alternate = true
                shootCost = 3f
                bullet = MissileBulletType(4f, 8f).apply {
                    homingPower = 0.08f
                    weaveMag = 4f
                    weaveScale = 4f
                    lifetime = 50f
                    keepVelocity = false
                    shootEffect = HoloFx.shootHeal
                    smokeEffect = HoloFx.hitLaser
                    despawnEffect = HoloFx.hitLaser
                    hitEffect = despawnEffect
                    frontColor = Color.white
                    hitSound = Sounds.none
                    healPercent = 5.5f
                    collidesTeam = true
                    backColor = R.C.Holo
                    trailColor = R.C.HoloDark
                }
            })
        }

        holoSupporter = NewUnitType(R.Unit.HoloSupporter, ::HoloUnitType, ::HoloUnit).apply {
            AutoLife(maxHealth = 4000f, lose = 0.15f)
            abilities.add(
                RepairFieldAbility(20f, 60f * 8, 60f).apply {
                    healEffect = HoloFx.heal
                    activeEffect = HoloFx.healWaveDynamic
                }
            )
            defaultController = Prov { RepairAI() }
            flying = true
            hovering = true
            HoloOpacity = 0.4f
            ColorOpacity = 0.3f
            armor = 2f
            speed = 2.5f
            accel = 0.06f
            drag = 0.017f
            buildSpeed = 2.2f
            hitSize = 14f
            engineSize = 2f
            engineOffset = 3f
            ammoType = PowerAmmoType(1100f)
            weapons.add(HoloWeapon((R.Unit.HoloSupporter + "-gun").Cio).apply {
                shootSound = Sounds.lasershoot
                reload = 15f
                x = 4f
                y = 5f
                rotate = true
                shootCost = 20f
                bullet = LaserBoltBulletType(5.2f, 15f).apply {
                    lifetime = 35f
                    healPercent = 8f
                    collidesTeam = true
                    backColor = R.C.HoloDark
                    frontColor = R.C.Holo
                    smokeEffect = HoloFx.hitLaser
                    hitEffect = HoloFx.hitLaser
                    despawnEffect = HoloFx.hitLaser
                    lightColor = R.C.Holo
                }
            })
        }
    }

    override fun load() {
    }

    override fun lastLoad() {
    }
}