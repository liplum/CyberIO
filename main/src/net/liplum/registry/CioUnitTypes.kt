package net.liplum.registry

import arc.func.Func
import arc.func.Prov
import arc.graphics.Color
import mindustry.Vars
import mindustry.ai.types.BuilderAI
import mindustry.ai.types.DefenderAI
import mindustry.ai.types.MinerAI
import mindustry.ai.types.RepairAI
import mindustry.content.Fx
import mindustry.content.Items
import mindustry.entities.abilities.RepairFieldAbility
import mindustry.entities.bullet.BasicBulletType
import mindustry.entities.bullet.LaserBoltBulletType
import mindustry.entities.bullet.MissileBulletType
import mindustry.gen.Sounds
import mindustry.type.Weapon
import mindustry.type.ammo.ItemAmmoType
import mindustry.type.ammo.PowerAmmoType
import mindustry.world.meta.BlockFlag
import net.liplum.*
import net.liplum.annotations.DependOn
import net.liplum.bullet.RuvikBullet
import net.liplum.bullet.STEM_VERSION
import net.liplum.flesh.BrainUnitType
import net.liplum.holo.*
import plumy.core.arc.minute
import net.liplum.mdt.utils.NewUnitType
import net.liplum.mdt.utils.plus
import net.liplum.mdt.utils.registerPayloadSource
import net.liplum.mdt.utils.registerUnitType
import net.liplum.script.NpcUnitType

object CioUnitTypes {
    @JvmStatic lateinit var holoMiner: HoloUnitType
    @JvmStatic lateinit var holoFighter: HoloUnitType
    @JvmStatic lateinit var holoGuardian: HoloUnitType
    @JvmStatic lateinit var holoArchitect: HoloUnitType
    @JvmStatic lateinit var holoSupporter: HoloUnitType
    @JvmStatic lateinit var brain: BrainUnitType
    @DependOn
    fun _preRegister() {
        HoloUnitType::class.java.registerPayloadSource()
        BrainUnitType::class.java.registerPayloadSource()
        NpcUnitType::class.java.registerPayloadSource()
    }
    @DependOn("CioItems.ic")
    fun holoMiner() {
        holoMiner = NewUnitType(R.Unit.HoloMiner, ::HoloUnitType, ::HoloUnit).apply {
            VanillaSpec {
                limitLife(hp = 1600f, lifespan = 3.minute)
                researchReq = arrayOf(
                    CioItems.ic + 1,
                    Items.titanium + 60,
                    Items.plastanium + 30,
                )
                mineTier = 4
            }
            ErekirSpec {
                limitLife(hp = 1600f, lifespan = 3.minute)
                researchReq = arrayOf(
                    CioItems.ic + 3,
                    Items.oxide + 20,
                    Items.carbide + 30,
                )
                mineTier = 5
            }
            speed = 2f
            targetPriority = -1f
            aiController = Prov { MinerAI() }
            controller = Func { MinerAI() }
            lowAltitude = true
            flying = true
            mineHardnessScaling = true
            hovering = true
            mineWalls = true
            mineFloor = true
            mineSpeed = 10f
            armor = 2f
            buildSpeed = 1f
            drag = 0.06f
            accel = 0.12f
            engineSize = 1.8f
            engineOffset = 5.7f
            range = 50f
            ammoType = PowerAmmoType(500f)
        }
    }
    @DependOn("CioItems.ic")
    fun holoFighter() {
        holoFighter = NewUnitType(R.Unit.HoloFighter, ::HoloUnitType, ::HoloUnit).apply {
            VanillaSpec {
                limitLife(hp = 3000f, lifespan = 15.minute)
                researchReq = arrayOf(
                    CioItems.ic + 2,
                    Items.titanium + 100,
                    Items.plastanium + 80,
                    Items.thorium + 60,
                )
            }
            ErekirSpec {
                limitLife(hp = 5000f, lifespan = 15.minute)
                researchReq = arrayOf(
                    CioItems.ic + 3,
                    Items.oxide + 20,
                    Items.carbide + 30,
                )
            }
            speed = 4f
            accel = 0.08f
            targetPriority = 1f
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
            targetFlags = arrayOf(BlockFlag.factory, null)
            circleTarget = true
            ammoType = ItemAmmoType(Items.plastanium)
            enableRuvikTip = true
            ruvikTipRange = 220f
            weapons.add(HoloWeapon("holo-fighter-gun".cio).apply {
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
                    hitSize = 10f
                    lifetime = 240f
                    trailColor = S.Hologram
                }
            })
        }
    }
    @DependOn("CioItems.ic")
    fun holoGuardian() {
        holoGuardian = NewUnitType(R.Unit.HoloGuardian, ::HoloUnitType, ::HoloUnit).apply {
            VanillaSpec {
                limitLife(hp = 5000f, lifespan = 10.minute)
                researchReq = arrayOf(
                    CioItems.ic + 1,
                    Items.titanium + 40,
                )
            }
            ErekirSpec {
                limitLife(hp = 8000f, lifespan = 10.minute)
                researchReq = arrayOf(
                    CioItems.ic + 3,
                    Items.oxide + 20,
                    Items.carbide + 30,
                )
            }
            abilities.add(
                HoloForceField(
                    60f, 4f, 2200f, 60f * 8
                )
            )
            targetPriority = 5f
            aiController = Prov { DefenderAI() }
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
    }
    @DependOn("CioItems.ic")
    fun holoArchitect() {
        holoArchitect = NewUnitType(R.Unit.HoloArchitect, ::HoloUnitType, ::HoloUnit).apply {
            VanillaSpec {
                limitLife(hp = 1200f, lifespan = 8.minute)
                buildSpeed = 4.6f
                speed = 3.5f
                researchReq = arrayOf(
                    CioItems.ic + 3,
                    Items.titanium + 120,
                    Items.plastanium + 160,
                    Items.thorium + 100,
                )
            }
            ErekirSpec {
                limitLife(hp = 1200f, lifespan = 7.5f.minute)
                buildSpeed = 3.6f
                speed = 3.0f
                researchReq = arrayOf(
                    CioItems.ic + 3,
                    Items.oxide + 20,
                    Items.carbide + 30,
                )
            }
            aiController = Prov { BuilderAI() }
            controller = Func { BuilderAI() }
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
            ammoType = PowerAmmoType(900f)
            weapons.add(HoloWeapon().apply {
                x = 0f
                y = 5f
                top = false
                reload = 30f
                ejectEffect = Fx.none
                recoil = 2f
                shoot.shots = 2
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
                    backColor = S.Hologram
                    trailColor = S.HologramDark
                }
            })
        }
    }
    @DependOn
    fun holoSupporter() {
        holoSupporter = NewUnitType(R.Unit.HoloSupporter, ::HoloUnitType, ::HoloUnit).apply {
            VanillaSpec {
                limitLife(hp = 3800f, lifespan = 12f.minute)
                buildSpeed = 2.2f
                speed = 2.5f
                accel = 0.06f
                drag = 0.017f
                ammoType = PowerAmmoType(1100f)
                researchReq = arrayOf(
                    CioItems.ic + 2,
                    Items.titanium + 80,
                    Items.plastanium + 120,
                )
                abilities.add(
                    RepairFieldAbility(20f, 60f * 8, 60f).apply {
                        healEffect = HoloFx.heal
                        activeEffect = HoloFx.healWaveDynamic
                    }
                )
            }
            ErekirSpec {
                limitLife(hp = 6000f, lifespan = 18f.minute)
                buildSpeed = 1.2f
                speed = 2.25f
                payloadCapacity = (5f * 5f) * Vars.tilePayload
                accel = 0.06f
                drag = 0.045f
                pickupUnits = true
                ammoType = PowerAmmoType(600f)
                researchReq = arrayOf(
                    CioItems.ic + 3,
                    Items.oxide + 20,
                    Items.carbide + 30,
                )
            }
            aiController = Prov { RepairAI() }
            controller = Func { RepairAI() }
            flying = true
            hovering = true
            HoloOpacity = 0.4f
            ColorOpacity = 0.3f
            armor = 2f
            hitSize = 14f
            engineSize = 2f
            engineOffset = 3f

            weapons.add(HoloWeapon((R.Unit.HoloSupporter + "-gun").cio).apply {
                shootSound = Sounds.lasershoot
                reload = 15f
                x = 4f
                y = 5f
                rotate = true
                shootCost = 20f
                bullet = LaserBoltBulletType(5.2f, 15f).apply {
                    lifetime = 35f
                    VanillaSpec {
                        healPercent = 8f
                    }
                    ErekirSpec {
                        healAmount = 50f
                    }
                    collidesTeam = true
                    backColor = S.HologramDark
                    frontColor = S.Hologram
                    smokeEffect = HoloFx.hitLaser
                    hitEffect = HoloFx.hitLaser
                    despawnEffect = HoloFx.hitLaser
                    lightColor = S.Hologram
                }
            })
        }
    }
    @DependOn
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