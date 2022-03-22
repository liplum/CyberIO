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
import mindustry.graphics.Pal
import mindustry.type.Weapon
import mindustry.type.ammo.ItemAmmoType
import mindustry.type.ammo.PowerAmmoType
import mindustry.world.meta.BlockFlag
import net.liplum.Cio
import net.liplum.R
import net.liplum.holo.HoloForceField
import net.liplum.holo.HoloUnit
import net.liplum.holo.HoloUnitType
import net.liplum.utils.NewUnitType

object CioUnitTypes : ContentTable {
    @JvmStatic lateinit var holoMiner: HoloUnitType
    @JvmStatic lateinit var holoFighter: HoloUnitType
    @JvmStatic lateinit var holoGuardian: HoloUnitType
    @JvmStatic lateinit var holoArchitect: HoloUnitType
    @JvmStatic lateinit var holoSupporter: HoloUnitType
    override fun firstLoad() {
        holoMiner = NewUnitType(R.Unit.HoloMiner, ::HoloUnitType, ::HoloUnit).apply {
            AutoLife(maxHealth = 2000f, lose = 0.08f)
            health = 2000f
            speed = 2f
            defaultController = Prov { MinerAI() }
            lowAltitude = true
            flying = true
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
            AutoLife(maxHealth = 3000f, lose = 0.3f)
            speed = 4f
            accel = 0.08f
            drag = 0.016f
            buildSpeed = 1f
            flying = true
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
            weapons.add(Weapon("holo-fighter-gun".Cio).apply {
                top = false
                shootSound = Sounds.flame
                shootY = 2f
                reload = 11f
                recoil = 1f
                ejectEffect = Fx.none
                bullet = CioBulletTypes.holoBullet
            })
        }

        holoGuardian = NewUnitType(R.Unit.HoloGuardian, ::HoloUnitType, ::HoloUnit).apply {
            AutoLife(maxHealth = 5000f, lose = 0.3f)
            abilities.add(
                HoloForceField(
                    60f, 4f, 2000f, 60f * 8
                )
            )
            defaultController = Prov { DefenderAI() }
            HoloOpacity = 0.4f
            speed = 1.6f
            flying = true
            buildSpeed = 2.6f
            drag = 0.05f
            accel = 0.1f
            rotateSpeed = 15f
            engineSize = 1.8f
            engineOffset = 5.7f
            hitSize = 15f
            armor = 5f
        }
        val holoArchitectWeapon = Weapon().apply {
            x = 0f
            y = 5f
            top = false
            reload = 30f
            ejectEffect = Fx.none
            recoil = 2f
            shootSound = Sounds.missile
            shots = 1
            velocityRnd = 0.5f
            inaccuracy = 15f
            alternate = true
            bullet = MissileBulletType(4f, 8f).apply {
                homingPower = 0.08f
                weaveMag = 4f
                weaveScale = 4f
                lifetime = 50f
                keepVelocity = false
                shootEffect = Fx.shootHeal
                smokeEffect = Fx.hitLaser
                despawnEffect = Fx.hitLaser
                hitEffect = despawnEffect
                frontColor = Color.white
                hitSound = Sounds.none
                healPercent = 5.5f
                collidesTeam = true
                backColor = Pal.heal
                trailColor = Pal.heal
            }
        }
        holoArchitect = NewUnitType(R.Unit.HoloArchitect, ::HoloUnitType, ::HoloUnit).apply {
            AutoLife(maxHealth = 2500f, lose = 0.1f)
            defaultController = Prov { BuilderAI() }
            speed = 3.5f
            HoloOpacity = 0.4f
            ColorOpacity = 0.05f
            flying = true
            drag = 0.06f
            accel = 0.12f
            lowAltitude = true
            engineSize = 1.8f
            engineOffset = 3.7f
            hitSize = 15f
            armor = 5f
            buildSpeed = 5f
            ammoType = PowerAmmoType(900f)

            weapons.add(
                holoArchitectWeapon,
                holoArchitectWeapon.copy()
            )
        }

        holoSupporter = NewUnitType(R.Unit.HoloSupporter, ::HoloUnitType, ::HoloUnit).apply {
            AutoLife(maxHealth = 4000f, lose = 0.15f)
            abilities.add(

                RepairFieldAbility(20f, 60f * 8, 60f)
            )
            defaultController = Prov { RepairAI() }
            flying = true
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
            weapons.add(Weapon((R.Unit.HoloSupporter + "-gun").Cio).apply {
                shootSound = Sounds.lasershoot
                reload = 15f
                x = 4f
                y = 5f
                rotate = true
                bullet = LaserBoltBulletType(5.2f, 15f).apply {
                    lifetime = 35f
                    healPercent = 6f
                    collidesTeam = true
                    backColor = Pal.heal
                    frontColor = Color.white
                }
            })
        }
    }

    override fun load() {
    }

    override fun lastLoad() {
    }
}