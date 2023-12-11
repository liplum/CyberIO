package net.liplum.registry

import arc.func.Func
import arc.func.Prov
import arc.graphics.Color
import arc.math.Angles
import arc.math.Mathf
import arc.math.geom.Geometry
import arc.math.geom.Vec2
import mindustry.Vars
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
import mindustry.graphics.Layer
import mindustry.type.Category
import mindustry.type.ammo.ItemAmmoType
import mindustry.type.ammo.PowerAmmoType
import mindustry.world.meta.BlockFlag
import mindustry.world.meta.BuildVisibility
import net.liplum.*
import net.liplum.annotations.DependOn
import net.liplum.bullet.RuvikBullet
import net.liplum.bullet.STEM_VERSION
import net.liplum.holo.*
import net.liplum.holo.HoloProjector.HoloProjectorBuild
import net.liplum.utils.NewUnitType
import net.liplum.render.DrawBuild
import net.liplum.render.SectionProgress
import net.liplum.render.progress
import net.liplum.render.regionSection
import plumy.core.arc.minute
import plumy.core.arc.plusAssign
import plumy.core.arc.second
import plumy.core.math.smoother
import plumy.dsl.DrawMulti
import plumy.dsl.plus

object CioHoloUnit {
    @JvmStatic
    lateinit var holoProjector: HoloProjector
    @JvmStatic
    lateinit var minerProjector: HoloProjector
    @JvmStatic
    lateinit var holoMiner: HoloUnitType
    @JvmStatic
    lateinit var holoFighter: HoloUnitType
    @JvmStatic
    lateinit var holoGuardian: HoloUnitType
    @JvmStatic
    lateinit var holoArchitect: HoloUnitType
    @JvmStatic
    lateinit var holoSupporter: HoloUnitType
    @DependOn(
        "CioItem.ic",
        "CioFluid.cyberion",
        "CioHoloUnit.holoMiner",
        "CioHoloUnit.holoFighter",
        "CioHoloUnit.holoGuardian",
        "CioHoloUnit.holoArchitect",
        "CioHoloUnit.holoSupporter",
    )
    fun holoProjector() {
        holoProjector = HoloProjector("holo-projector").apply {
            category = Category.units
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    CioItem.ic + 80,
                    Items.silicon + 220,
                    Items.graphite + 300,
                    Items.metaglass + 500,
                    Items.thorium + 1200,
                )
                liquidCapacity = 100f
                scaledHealth = 100f
                researchCostMultiplier = 0.8f
                consumePower(3f)
                planning {
                    holoMiner needs those(
                        item = CioItem.ic + 2,
                        cyberion = 3.0f / 60f,
                        time = 16f.second,
                    )
                    holoFighter needs those(
                        item = CioItem.ic + 1,
                        cyberion = 3.0f / 60f,
                        time = 15f.second,
                    )
                    holoGuardian needs those(
                        item = CioItem.ic + 1,
                        cyberion = 1.5f / 60f,
                        time = 7.5f.second,
                    )
                    holoArchitect needs those(
                        item = CioItem.ic + 3,
                        cyberion = 6.0f / 60f,
                        time = 25f.second,
                    )
                    holoSupporter needs those(
                        item = CioItem.ic + 1,
                        cyberion = 2.5f / 60f,
                        time = 12f.second,
                    )
                }
            }
            ErekirSpec {
                requirements = arrayOf(
                    CioItem.ic + 100,
                    Items.oxide + 220,
                    Items.thorium + 300,
                    Items.carbide + 120,
                    Items.silicon + 800,
                )
                liquidCapacity = 100f
                scaledHealth = 150f
                researchCostMultiplier = 0.75f
                consumePower(3f)
                planning {
                    holoMiner needs those(
                        item = CioItem.ic + 2,
                        cyberion = 1.5f / 60f,
                        time = 18f.second,
                    )
                    holoFighter needs those(
                        item = CioItem.ic + 1,
                        cyberion = 3.0f / 60f,
                        time = 15f.second,
                    )
                    holoGuardian needs those(
                        item = CioItem.ic + 1,
                        cyberion = 1.0f / 60f,
                        time = 10f.second,
                    )
                    holoArchitect needs those(
                        item = CioItem.ic + 2,
                        cyberion = 3.0f / 60f,
                        time = 25f.second,
                    )
                    holoSupporter needs those(
                        item = CioItem.ic + 3,
                        cyberion = 5f / 60f,
                        time = 18f.second,
                    )
                }
            }
            drawer = DrawMulti {
                +DrawBuild<HoloProjectorBuild> {
                    val v = Vec2()
                    val focus: HoloProjectorBuild.() -> Vec2 = {
                        val len = 3.8f + Mathf.absin(projecting, 3.0f, 0.6f)
                        val x = x + Angles.trnsx(projecting, len)
                        val y = y + Angles.trnsy(projecting, len)
                        v.set(x, y)
                    }
                    val prog: SectionProgress<HoloProjectorBuild> = { preparing.smoother }
                    val sprites = listOf(
                        "-top-r",
                        "-top-l",
                        "-bottom-l",
                        "-bottom-r",
                    )
                    for ((i, sprite) in sprites.withIndex()) {
                        regionSection(sprite) {
                            val quadrant = Geometry.d8edge[i]
                            layer = Layer.blockOver
                            progress = prog
                            shadowProgress = prog
                            shadowElevation = 1.5f
                            moveX = 5f * quadrant.x
                            moveY = 5f * quadrant.y
                            holoProjectingSection {
                                isTopRightOrBottomLeft = i % 2 == 0
                                center = focus
                                x = 8f * quadrant.x
                                y = 8f * quadrant.y
                            }
                        }
                    }
                }
                +DrawProjectingHoloUnit()
            }
            ambientSound = Sounds.build
            squareSprite = false
            size = 5
            buildCostMultiplier = 2f
        }
    }
    @DependOn(
        "CioItem.ic",
        "CioFluid.cyberion",
        "CioHoloUnit.holoMiner",
    )
    fun minerProjector() {
        DebugOnly {
            minerProjector = HoloProjector("miner-projector").apply {
                category = Category.production
                buildVisibility = BuildVisibility.shown
                planning {
                    holoMiner needs those(
                        item = CioItem.ic + 2,
                        cyberion = 3.0f / 60f,
                        time = 16f.second,
                    )
                }
                drawer = DrawMulti {
                    +DrawBuild<HoloProjectorBuild> {
                        val v = Vec2()
                        val focus: HoloProjectorBuild.() -> Vec2 = {
                            val len = 3.8f + Mathf.absin(projecting, 3.0f, 0.6f)
                            val x = x + Angles.trnsx(projecting, len)
                            val y = y + Angles.trnsy(projecting, len)
                            v.set(x, y)
                        }
                        projectorSection("-top") {
                            layer = Layer.blockOver
                            progress = progress { preparing.smoother }
                            shadowElevation = 1f
                            y = 9f
                            moveX = 0f
                            moveY = 12f
                            rotation = { 0f }
                            holoProjectingSection {
                                center = focus
                                x = 8f
                                y = 8f
                            }
                        }
                    }
                    +DrawProjectingHoloUnit()
                }
                ambientSound = Sounds.build
                squareSprite = false
                size = 3
                buildCostMultiplier = 1.2f
            }
        }
    }
    @DependOn("CioItem.ic")
    fun holoMiner() {
        holoMiner = NewUnitType(R.Unit.HoloMiner, ::HoloUnitType, ::HoloUnit) {
            VanillaSpec {
                limitLife(hp = 1600f, lifespan = 3.minute)
                researchReq = arrayOf(
                    CioItem.ic + 1,
                    Items.titanium + 60,
                    Items.plastanium + 30,
                )
                mineTier = 4
            }
            ErekirSpec {
                limitLife(hp = 1600f, lifespan = 3.minute)
                researchReq = arrayOf(
                    CioItem.ic + 3,
                    Items.oxide + 20,
                )
                mineTier = 5
            }
            itemCapacity = 100
            isEnemy = false
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
    @DependOn("CioItem.ic")
    fun holoFighter() {
        holoFighter = NewUnitType(R.Unit.HoloFighter, ::HoloUnitType, ::HoloUnit) {
            VanillaSpec {
                limitLife(hp = 3000f, lifespan = 15.minute)
                researchReq = arrayOf(
                    CioItem.ic + 2,
                    Items.titanium + 100,
                    Items.plastanium + 80,
                    Items.thorium + 60,
                )
                drag = 0.03f
                speed = 4f
            }
            ErekirSpec {
                limitLife(hp = 5000f, lifespan = 15.minute)
                researchReq = arrayOf(
                    CioItem.ic + 3,
                    Items.oxide + 20,
                )
                drag = 0.05f
                speed = 3.8f
            }
            isEnemy = false
            accel = 0.08f
            targetPriority = 1f
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
            weapons += HoloWeapon("holo-fighter-gun".cio).apply {
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
                    trailColor = Var.Hologram
                }
            }
        }
    }
    @DependOn("CioItem.ic")
    fun holoGuardian() {
        holoGuardian = NewUnitType(R.Unit.HoloGuardian, ::HoloUnitType, ::HoloUnit) {
            VanillaSpec {
                limitLife(hp = 5000f, lifespan = 10.minute)
                researchReq = arrayOf(
                    CioItem.ic + 1,
                    Items.titanium + 40,
                )
            }
            ErekirSpec {
                limitLife(hp = 8000f, lifespan = 10.minute)
                researchReq = arrayOf(
                    CioItem.ic + 3,
                    Items.oxide + 20,
                )
            }
            abilities.add(
                HoloForceField(
                    60f, 4f, 2200f, 60f * 8
                )
            )
            isEnemy = false
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
    @DependOn("CioItem.ic")
    fun holoArchitect() {
        holoArchitect = NewUnitType(R.Unit.HoloArchitect, ::HoloUnitType, ::HoloUnit) {
            VanillaSpec {
                limitLife(hp = 1200f, lifespan = 8.minute)
                buildSpeed = 4.6f
                speed = 3.5f
                researchReq = arrayOf(
                    CioItem.ic + 3,
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
                    CioItem.ic + 3,
                    Items.oxide + 20,
                )
            }
            isEnemy = false
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
            weapons += HoloWeapon().apply {
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
                    backColor = Var.Hologram
                    trailColor = Var.HologramDark
                }
            }
        }
    }
    @DependOn("CioItem.ic")
    fun holoSupporter() {
        holoSupporter = NewUnitType(R.Unit.HoloSupporter, ::HoloUnitType, ::HoloUnit) {
            VanillaSpec {
                limitLife(hp = 3800f, lifespan = 12f.minute)
                buildSpeed = 2.2f
                speed = 2.5f
                accel = 0.06f
                drag = 0.017f
                ammoType = PowerAmmoType(1100f)
                researchReq = arrayOf(
                    CioItem.ic + 2,
                    Items.titanium + 80,
                    Items.plastanium + 120,
                )
                abilities += RepairFieldAbility(20f, 60f * 8, 60f).apply {
                    healEffect = HoloFx.heal
                    activeEffect = HoloFx.healWaveDynamic
                }
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
                    CioItem.ic + 3,
                    Items.oxide + 20,
                )
            }
            isEnemy = false
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

            weapons += HoloWeapon((R.Unit.HoloSupporter + "-gun").cio).apply {
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
                    backColor = Var.HologramDark
                    frontColor = Var.Hologram
                    smokeEffect = HoloFx.hitLaser
                    hitEffect = HoloFx.hitLaser
                    despawnEffect = HoloFx.hitLaser
                    lightColor = Var.Hologram
                }
            }
        }
    }
}