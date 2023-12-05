package net.liplum.registry

import arc.func.Prov
import arc.graphics.Color
import arc.math.Interp
import arc.util.Time
import mindustry.Vars
import mindustry.content.Fx
import mindustry.content.Items
import mindustry.content.Liquids
import mindustry.content.StatusEffects
import mindustry.entities.bullet.BasicBulletType
import mindustry.entities.part.DrawPart.PartProgress
import mindustry.entities.pattern.ShootAlternate
import mindustry.entities.pattern.ShootSpread
import mindustry.game.EventType.Trigger
import mindustry.gen.Sounds
import mindustry.graphics.Layer
import mindustry.type.Category
import mindustry.world.blocks.defense.turrets.PowerTurret.PowerTurretBuild
import mindustry.world.blocks.environment.Floor
import mindustry.world.blocks.heat.HeatProducer
import mindustry.world.blocks.production.GenericCrafter
import mindustry.world.blocks.production.HeatCrafter
import mindustry.world.draw.DrawLiquidTile
import mindustry.world.meta.BuildVisibility
import net.liplum.*
import net.liplum.annotations.DependOn
import net.liplum.annotations.Only
import net.liplum.annotations.Subscribe
import net.liplum.api.virus.setUninfected
import net.liplum.api.virus.setUninfectedFloor
import net.liplum.blocks.bomb.ZipBomb
import net.liplum.blocks.cyberion.DrawCyberionAgglomeration
import net.liplum.blocks.deleter.Deleter
import net.liplum.blocks.deleter.DeleterWave
import net.liplum.blocks.deleter.deleted
import net.liplum.blocks.jammer.Jammer
import net.liplum.blocks.jammer.JammingLaser
import net.liplum.blocks.power.WirelessTower
import net.liplum.blocks.prism.Prism
import net.liplum.blocks.prism.PrismObelisk
import net.liplum.blocks.tmtrainer.CharBulletType
import net.liplum.blocks.tmtrainer.RandomName
import net.liplum.blocks.tmtrainer.TMTRAINER
import net.liplum.blocks.tmtrainer.TMTRAINER.DrawCore
import net.liplum.blocks.underdrive.UnderdriveProjector
import net.liplum.blocks.virus.AntiVirus
import net.liplum.blocks.virus.Virus
import net.liplum.bullet.RuvikBullet
import net.liplum.bullet.STEM_VERSION
import net.liplum.bullet.ShaderBasicBulletT
import net.liplum.common.shader.ShaderBase
import net.liplum.holo.HoloFloor
import net.liplum.holo.HoloWall
import net.liplum.holo.LandProjector
import net.liplum.holo.Stealth
import net.liplum.render.DrawTurretHeat
import net.liplum.ui.DynamicContentInfoDialog.Companion.registerDynamicInfo
import net.liplum.registry.CioBulletType.optInRadiationInterference
import net.liplum.registry.CioBulletType.optInVirus
import net.liplum.render.*
import net.liplum.statusFx.StaticFx
import net.liplum.utils.globalAnim
import plumy.core.Else
import plumy.core.math.invoke
import plumy.core.math.smooth
import plumy.dsl.*

object CioBlock {
    @JvmStatic lateinit var icAssembler: GenericCrafter
    @JvmStatic lateinit var virus: Virus
    @JvmStatic lateinit var landProjector: LandProjector
    @JvmStatic lateinit var holoFloor: HoloFloor
    @JvmStatic lateinit var underdriveProjector: UnderdriveProjector
    @JvmStatic lateinit var antiVirus: AntiVirus
    @JvmStatic lateinit var prism: Prism
    @JvmStatic lateinit var prismObelisk: PrismObelisk
    @JvmStatic lateinit var deleter: Deleter
    @JvmStatic lateinit var holoWall: HoloWall
    @JvmStatic lateinit var holoWallLarge: HoloWall
    @JvmStatic lateinit var TMTRAINER: TMTRAINER
    @JvmStatic lateinit var jammer: Jammer
    @JvmStatic lateinit var cyberionMixer: GenericCrafter
    @JvmStatic lateinit var aquacyberion: Floor
    @JvmStatic lateinit var stealth: Stealth
    @JvmStatic lateinit var wirelessTower: WirelessTower
    @JvmStatic lateinit var zipBomb: ZipBomb
    @DependOn("CioItem.ic")
    fun icAssembler() {
        icAssembler = GenericCrafter("ic-assembler").apply {
            category = Category.crafting
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    Items.lead + 150,
                    Items.graphite + 100,
                    Items.silicon + 80,
                )
                scaledHealth = 80f
                consumePower(1.2f)
                itemCapacity = 40
                consumeItems(
                    Items.copper + 4,
                    Items.silicon + 3,
                    Items.metaglass + 2,
                )
                craftTime = 175f
                drawMulti {
                    +DrawRegionSpec("-bottom")
                    +SpecDrawConstruct(stages = 3)
                    +DrawDefaultSpec()
                }
            }
            ErekirSpec {
                requirements = arrayOf(
                    Items.beryllium + 60,
                    Items.graphite + 105,
                    Items.tungsten + 50,
                    Items.silicon + 200,
                )
                scaledHealth = 120f
                consumePower(1.1f)
                itemCapacity = 40
                consumeItems(
                    Items.tungsten + 2,
                    Items.beryllium + 3,
                    Items.silicon + 4,
                )
                craftTime = 200f
                drawMulti {
                    +DrawRegionSpec("-bottom")
                    +SpecDrawConstruct(stages = 4)
                    +DrawDefaultSpec()
                }
                squareSprite = false
            }
            buildType = Prov { GenericCrafterBuild() }
            fogRadius = 2
            size = 2
            outputItem = CioItem.ic + 1
            craftEffect = Fx.smeltsmoke
        }
    }
    @DependOn
    fun virus() {
        virus = Virus("virus").apply {
            category = Category.effect
            buildVisibility = BuildVisibility.sandboxOnly
            requirements = arrayOf(
                Items.sporePod + 50,
                Items.pyratite + 20,
            )
            buildCostMultiplier = 5f
            spreadingSpeed = 200
            maxReproductionScale = 20
            maxGeneration = 100
            inheritChildrenNumber = false
            mutationRate = 10
        }.globalAnim(30f, 3)
    }
    @DependOn("CioItem.ic")
    fun landProjector() {
        landProjector = LandProjector("land-projector").apply {
            category = Category.effect
            DebugOnly {
                buildVisibility = BuildVisibility.shown
            }.Else {
                buildVisibility = BuildVisibility.hidden
            }
            requirements = arrayOf(
                CioItem.ic + 150,
                Items.graphite + 80,
                Items.thorium + 100,
                Items.silicon + 50
            )
            size = 2
            buildCostMultiplier = 3f
        }
    }
    @DependOn
    fun holoFloor() {
        holoFloor = HoloFloor("holo-floor").apply {
            variants = 3
        }.setUninfectedFloor()
    }
    @DependOn("CioItem.ic")
    fun underdriveProjector() {
        underdriveProjector = UnderdriveProjector("underdrive-projector").apply {
            category = Category.power
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    CioItem.ic + 50,
                    Items.copper + 300,
                    Items.lead + 20,
                    Items.silicon + 240,
                    Items.plastanium + 10,
                    Items.phaseFabric + 5,
                )
                scaledHealth = 350f
                powerProduction = 4.5f
                maxPowerEFFBlocksReq = 22
                slowDownRateEFFReward = 0.6f
                maxGear = 5
            }
            ErekirSpec {
                requirements = arrayOf(
                    CioItem.ic + 25,
                    Items.tungsten + 15,
                    Items.silicon + 50,
                    Items.beryllium + 80,
                )
                scaledHealth = 250f
                range = 45f
                slowDownRateEFFReward = 1.2f
                powerProduction = 2.5f
                maxPowerEFFBlocksReq = 18
                maxGear = 8
            }
            color = R.C.LightBlue
            maxSlowDownRate = 0.9f
            size = 1
        }
    }
    @DependOn("CioItem.ic")
    fun antiVirus() {
        antiVirus = AntiVirus("anti-virus").apply {
            category = Category.effect
            buildVisibility = BuildVisibility.shown
            requirements = arrayOf(
                CioItem.ic + 10,
                Items.copper + 100,
                Items.graphite + 40,
                Items.silicon + 25
            )
            scaledHealth = 120f
            consumePower(0.5f)
            size = 1
        }.setUninfected()
    }
    @DependOn("CioItem.ic")
    fun prism() {
        prism = Prism("prism").apply {
            category = Category.turret
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    CioItem.ic + 130,
                    Items.copper + 250,
                    Items.metaglass + 350,
                    Items.titanium + 50,
                )
                scaledHealth = 150f
            }
            ErekirSpec {
                requirements = arrayOf(
                    CioItem.ic + 120,
                    Items.carbide + 100,
                    Items.beryllium + 250,
                    Items.tungsten + 250,
                )
                scaledHealth = 125f
            }
            crystalSounds = CioSound.crystal
            crystalSoundVolume = 0.8f
            buildCostMultiplier = 2f
            size = 4
        }
    }
    @DependOn(
        "CioItem.ic",
        "CioBlock.prism"
    )
    fun prismObelisk() {
        prismObelisk = PrismObelisk("prism-obelisk").apply {
            category = Category.turret
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    CioItem.ic + 50,
                    Items.copper + 60,
                    Items.plastanium + 120,
                    Items.metaglass + 240,
                    Items.titanium + 10,
                )
                scaledHealth = 160f
            }
            ErekirSpec {
                requirements = arrayOf(
                    CioItem.ic + 45,
                    Items.beryllium + 60,
                    Items.carbide + 50,
                    Items.tungsten + 100,
                )
                scaledHealth = 180f
            }
            size = 2
            prismType = prism
        }
    }
    @DependOn("CioItem.ic")
    fun deleter() {
        deleter = Deleter("deleter").apply {
            category = Category.turret
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    CioItem.ic + 80,
                    Items.graphite + 100,
                    Items.silicon + 60,
                    Items.thorium + 250,
                    Items.surgeAlloy + 50,
                )
                cooldownTime = 20f
                recoil = 5f
                range = 180f
                reload = 15f
                consumePower(3f)
                extraLostHpBounce = 0.005f
                scaledHealth = 200f
                executeProportion = 0.2f
                shoot = ShootSpread().apply {
                    shots = 18
                    spread = 3f
                }
            }
            ErekirSpec {
                requirements = arrayOf(
                    CioItem.ic + 75,
                    Items.surgeAlloy + 80,
                    Items.tungsten + 120,
                    Items.carbide + 30,
                )
                cooldownTime = 18f
                recoil = 5f
                range = 200f
                reload = 20f
                consumePower(4f)
                extraLostHpBounce = 0.003f
                scaledHealth = 200f
                scaledHealth = 80f
                executeProportion = 0.18f
                shoot = ShootSpread().apply {
                    shots = 18
                    spread = 3f
                }
            }
            targetAir = true
            targetGround = true
            size = 3
            buildCostMultiplier = 1.5f
            shootSound = Sounds.lasershoot
            VanillaSpec {
                minWarmup = 0.96f
                shootWarmupSpeed = 0.03f
            }
            ErekirSpec {
                minWarmup = 0.96f
                shootWarmupSpeed = 0.06f
            }
            shootType = DeleterWave(executeProportion, extraLostHpBounce).apply {
                deletedFx = deleted
                shootEffect = Fx.none
                smokeEffect = Fx.none
                damage = 1.5f
                pierceCap = 3
            }
            drawMulti {
                drawTurret {
                    regionPart("-side") {
                        heatProgress = PartProgress.warmup
                        heatColor = Var.Hologram
                        VanillaSpec {
                            progress = PartProgress.warmup
                            moveX = 8f
                            moveRot = 40f
                        }
                        ErekirSpec {
                            progress = PartProgress { Interp.pow3Out(it.warmup) }
                            moveY = 15f
                            moveX = -5f
                            moveRot = -170f
                        }
                        mirror = true
                    }
                    regionPart("-head") {
                        heatProgress = PartProgress.warmup
                        heatColor = Var.Hologram
                        VanillaSpec {
                            progress = PartProgress { it.warmup.smooth }
                            moveY = 3f
                            moveRot = -20f
                            addMove(
                                progress = {
                                    if (it.recoil == 0f) 0f else it.smoothReload
                                },
                                x = -1.5f
                            )
                        }
                        ErekirSpec {
                            progress = PartProgress { it.warmup.smooth }
                            moveY = 5f
                            moveRot = -30f
                            addMove(
                                progress = {
                                    if (it.recoil == 0f) 0f else it.smoothReload
                                },
                                x = -2f
                            )
                        }
                        under = true
                        mirror = true
                    }
                }
                +DrawTurretHeat<PowerTurretBuild>("-glow") { warmup() }
            }
        }
    }
    @DependOn("CioItem.ic")
    fun holoWall() {
        holoWall = HoloWall("holo-wall").apply {
            category = Category.defense
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    CioItem.ic + 32,
                    Items.silicon + 6,
                    Items.titanium + 12,
                    Items.plastanium + 10,
                )
                scaledHealth = 1000f
                restoreCharge = 10 * 60f
            }
            ErekirSpec {
                requirements = arrayOf(
                    CioItem.ic + 30,
                    Items.beryllium + 50,
                    Items.tungsten + 25,
                    Items.carbide + 4,
                )
                scaledHealth = 800f
                restoreCharge = 15 * 60f
                needPower = true
                powerCapacity = 300f
                powerUseForChargePreUnit = 0.2f
            }
            lightColor = Var.Hologram
            lightRadius = 40f
            floatingRange = 1f
            size = 1
            buildCostMultiplier = 3.5f
        }
    }
    @DependOn(
        "CioItem.ic",
        "CioBlock.holoWall"
    )
    fun holoWallLarge() {
        holoWallLarge = HoloWall("holo-wall-large").apply {
            category = Category.defense
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    CioItem.ic + 58,
                    Items.silicon + 80,
                    Items.titanium + 48,
                    Items.plastanium + 40,
                )
                restoreCharge = 15 * 60f
                scaledHealth = 820f
            }
            ErekirSpec {
                requirements = arrayOf(
                    CioItem.ic + 56,
                    Items.beryllium + 100,
                    Items.tungsten + 55,
                    Items.carbide + 8,
                )
                restoreCharge = 20 * 60f
                scaledHealth = 700f
                needPower = true
                powerCapacity = 800f
                powerUseForChargePreUnit = 0.3f
            }
            lightColor = Var.Hologram
            lightRadius = 80f
            floatingRange = 2f
            squareSprite = false
            size = 2
            buildCostMultiplier = 4.5f
        }
    }
    @DependOn(
        "CioItem.ic",
        "CioSEffect.infected",
        "CioSEffect.static",
    )
    fun TMTRAINER() {
        TMTRAINER = TMTRAINER("TMTRAINER").apply {
            category = Category.turret
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    CioItem.ic + 38,
                    Items.titanium + 100,
                    Items.graphite + 100,
                    Items.silicon + 50,
                )
                addAmmo(Items.sporePod, BasicBulletType().apply {
                    speed = 2.5f
                    damage = 50f
                    width = 10f
                    height = 12f
                    shrinkY = 0.1f
                    lifetime = 100f
                    hitSize = 10f
                    optInVirus()
                })
                addAmmo(Items.thorium, ShaderBasicBulletT<ShaderBase>().apply {
                    speed = 2.3f
                    damage = 40f
                    width = 15f
                    height = 15f
                    hitSize = 15f
                    lifetime = 120f
                    pierce = true
                    pierceCap = 2
                    optInRadiationInterference()
                })
                addAmmo(CioItem.ic, CharBulletType().apply {
                    speed = 2.55f
                    damage = 80f
                    lifetime = 180f
                    hitSize = 5f
                    ammoMultiplier = 5f
                    reloadMultiplier = 0.75f
                    homingDelay = 20f
                    homingRange = 340f
                    homingPower = 4f
                })
                shoot = ShootAlternate().apply {
                    spread = 4f
                }
                scaledHealth = 250f
            }
            ErekirSpec {
                requirements = arrayOf(
                    CioItem.ic + 35,
                    Items.tungsten + 180,
                    Items.oxide + 80,
                )
                addAmmo(Items.tungsten, BasicBulletType().apply {
                    speed = 2.8f
                    damage = 180f
                    width = 10f
                    height = 12f
                    shrinkY = 0.1f
                    lifetime = 100f
                    hitSize = 10f
                    optInVirus()
                })
                addAmmo(Items.thorium, ShaderBasicBulletT<ShaderBase>().apply {
                    speed = 2.25f
                    damage = 280f
                    width = 15f
                    height = 15f
                    hitSize = 15f
                    lifetime = 120f
                    pierce = true
                    pierceCap = 2
                    optInRadiationInterference()
                })
                addAmmo(CioItem.ic, CharBulletType().apply {
                    speed = 2.4f
                    damage = 150f
                    lifetime = 180f
                    hitSize = 5f
                    ammoMultiplier = 5f
                    reloadMultiplier = 0.8f
                    homingDelay = 15f
                    homingRange = 240f
                    homingPower = 3f
                })
                shoot = ShootAlternate().apply {
                    spread = 6f
                }
                scaledHealth = 270f
            }
            inaccuracy = 1f
            rotateSpeed = 10f
            maxAmmo = 80
            reload = 5f
            range = 260f
            shootCone = 15f
            size = 4
            shootWarmupSpeed = 0.05f
            drawMulti {
                drawTurret {
                    regionPart("-head") {
                        y = 14f
                        progress = PartProgress { Interp.pow10In(it.warmup) }
                        moveY = -6f
                        under = true
                    }
                }
                +DrawCore()
            }
        }.registerDynamicInfo()
    }
    @Subscribe(Trigger.update, Only.client)
    fun TMTRAINER_RandomName() {
        if (Time.globalTime % Var.AnimUpdateFrequency < 1f && CioMod.ContentLoaded) {
            TMTRAINER.localizedName = RandomName.one(8)
            TMTRAINER.description = RandomName.one(25)
        }
    }
    @DependOn(
        "CioItem.ic",
        "CioFluid.cyberion",
    )
    fun jammer() {
        jammer = Jammer("jammer").apply {
            category = Category.turret
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    CioItem.ic + 55,
                    Items.lead + 350,
                    Items.thorium + 200,
                    Items.surgeAlloy + 150,
                )
                scaledHealth = 250f
                consumePower(8f)
                range = 195f
                liquidConsumed = 12f / 60f
            }
            ErekirSpec {
                requirements = arrayOf(
                    CioItem.ic + 80,
                    Items.surgeAlloy + 150,
                    Items.thorium + 320,
                )
                scaledHealth = 350f
                range = 165f
                liquidConsumed = 10f / 60f
            }
            size = 3
            shootEffect = StaticFx
            shake = 2f
            reload = 240f
            shoot.firstShotDelay = 90f
            chargeSound = CioSound.jammerPreShoot
            shootSound = Sounds.none
            loopSound = CioSound.tvStatic
            loopSoundVolume = 0.3f
            rotateSpeed = 2f
            addAmmo(CioFluid.cyberion, JammingLaser {
                VanillaSpec {
                    damage = 100f
                    length = 220f
                    drawSize = 300f
                }
                ErekirSpec {
                    damage = 250f
                    length = 180f
                    drawSize = 280f
                }
                width = 6f
                divisions = 5
                hitEffect = StaticFx
                hitColor = Color.white
                status = CioSEffect.static
                incendChance = 0.4f
                incendSpread = 5f
                incendAmount = 1
                ammoMultiplier = 1f
            })
            shootWarmupSpeed = 0.03f
            minWarmup = 0.96f
            shootY = -3f
            drawMulti {
                drawTurret {
                    regionPart("-barrel") {
                        mirror = true
                        under = true
                        progress = PartProgress.warmup
                        moveX = 7f
                        moveY = -1.8f
                        x = 1.2f
                    }
                    shapePart {
                        circle = true
                        hollow = true
                        y = -8.5f
                        radius = 2.5f
                        color = R.C.FutureBlue
                        layer = Layer.effect
                    }
                    haloPart {
                        y = -8.5f
                        haloRadius = 3.5f
                        radius = 1.2f
                        radiusTo = 15f
                        haloRotateSpeed = 5f
                        haloRadiusTo = 360f
                        progress = PartProgress {
                            (if (it.heat > 0f) 0f else 1f) * Interp.pow10In(it.recoil)
                        }
                        color = R.C.FutureBlue
                        layer = Layer.effect
                    }
                    regionPart("-side") {
                        mirror = true
                        progress = PartProgress.warmup
                        moveX = 4f
                        moveY = -4f
                    }
                }
                +DrawStereo()
            }
        }
    }
    @DependOn(
        "CioItem.ic",
        "CioFluid.cyberion"
    )
    fun cyberionMixer() {
        VanillaSpec {
            cyberionMixer = HeatProducer("cyberion-mixer").apply {
                category = Category.crafting
                buildVisibility = BuildVisibility.shown
                requirements = arrayOf(
                    CioItem.ic + 30,
                    Items.lead + 100,
                    Items.titanium + 100,
                    Items.metaglass + 50,
                )
                buildType = Prov { HeatProducerBuild() }
                scaledHealth = 60f
                liquidCapacity = 100f
                craftTime = 100f
                squareSprite = false
                consumePower(1.5f)
                consumeItem(Items.thorium, 1)
                consumeLiquid(Liquids.cryofluid, 0.3f)
                outputLiquid = CioFluid.cyberion + 0.25f
                heatOutput = 3f
                drawMulti {
                    +DrawRegionSpec("-bottom")
                    +DrawLiquidTile(Liquids.cryofluid, 3f)
                    +DrawLiquidTile(CioFluid.cyberion, 3f)
                    +DrawDefaultSpec()
                    +DrawHeatOutputSpec().apply {
                        heatColor = Var.Hologram
                    }
                    +DrawCyberionAgglomeration()
                }
                size = 3
            }
        }
        ErekirSpec {
            cyberionMixer = HeatCrafter("cyberion-mixer").apply {
                category = Category.crafting
                buildVisibility = BuildVisibility.shown
                requirements = arrayOf(
                    CioItem.ic + 30,
                    Items.oxide + 70,
                    Items.tungsten + 120,
                )
                buildType = Prov { HeatCrafterBuild() }
                scaledHealth = 100f
                liquidCapacity = 200f
                craftTime = 90f
                squareSprite = false
                consumeLiquid(Liquids.slag, 0.15f)
                consumeItem(Items.oxide, 1)
                heatRequirement = 8f
                overheatScale = 1.5f
                outputLiquid = CioFluid.cyberion + 0.25f
                drawMulti {
                    +DrawRegionSpec("-bottom")
                    +DrawPistonsSpec().apply {
                        sinMag = 3f
                        sinScl = 5f
                    }
                    +DrawGlowRegionSpec()
                    +DrawDefaultSpec()
                    +DrawLiquidTile(Liquids.slag, 37f / 4f)
                    +DrawLiquidTile(CioFluid.cyberion, 37f / 4f)
                    +DrawRegionSpec("-top")
                    +DrawHeatInputSpec().apply {
                        heatColor = Var.Hologram
                    }
                }
                size = 3
            }
        }
    }
    @DependOn("CioFluid.cyberion")
    fun aquacyberion() {
        aquacyberion = Floor("aqua-cyberion").apply {
            drownTime = 0f
            VanillaSpec {
                status = StatusEffects.freezing
                speedMultiplier = 0.1f
            }
            ErekirSpec {
                status = StatusEffects.melting
                speedMultiplier = 0.5f
            }
            statusDuration = 240f
            variants = 0
            liquidDrop = CioFluid.cyberion
            liquidMultiplier = 0.1f
            isLiquid = true
            cacheLayer = CioCLs.cyberion
            emitLight = true
            lightRadius = 30f
            lightColor = Var.Hologram.cpy().a(0.19f)
        }
    }
    @DependOn(
        "CioItem.ic",
        "CioFluid.cyberion"
    )
    fun stealth() {
        stealth = Stealth("stealth").apply {
            category = Category.turret
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    CioItem.ic + 30,
                    Items.titanium + 150,
                    Items.plastanium + 50,
                )
                scaledHealth = 160f
                range = 260f
                liquidCapacity = 60f
                recoil = 3f
                reload = 15f
            }
            ErekirSpec {
                requirements = arrayOf(
                    CioItem.ic + 35,
                    Items.beryllium + 240,
                    Items.tungsten + 60,
                )
                scaledHealth = 140f
                range = 270f
                liquidCapacity = 50f
                recoil = 3.5f
                reload = 18f
            }
            shootSound = Sounds.lasershoot
            size = 3
            squareSprite = false

            shootType = RuvikBullet bullet@{
                VanillaSpec {
                    speed = 2f
                    damage = 110f
                }
                ErekirSpec {
                    speed = 2.5f
                    damage = 220f
                }
                stemVersion = STEM_VERSION.STEM2
                smokeEffect = Fx.none
                shootSound = Sounds.none
                arrowWidth = 10f
                hitSize = 10f
                lifetime = 240f
                maxRange = this@apply.range
                trailColor = Var.Hologram
            }
        }
    }
    @DependOn("CioItem.ic")
    fun wirelessTower() {
        wirelessTower = WirelessTower("wireless-tower").apply {
            category = Category.power
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    CioItem.ic + 30,
                    Items.copper + 200,
                    Items.lead + 20,
                    Items.silicon + 20,
                    Items.graphite + 30,
                )
                dstExtraPowerConsumeFactor = 1.0f
                reactivePower = 0.05f
                scaledHealth = 150f
                distributeSpeed = 15f
                range = 520f
            }
            ErekirSpec {
                requirements = arrayOf(
                    CioItem.ic + 50,
                    Items.tungsten + 150,
                    Items.graphite + 100,
                    Items.silicon + 80,
                    Items.carbide + 30,
                )
                dstExtraPowerConsumeFactor = 0.6f
                reactivePower = 0.03f
                scaledHealth = 200f
                distributeSpeed = 20f
                range = 580f
            }
            size = 2
        }
    }
    @DependOn("CioItem.ic")
    fun zipBomb() {
        zipBomb = ZipBomb("zip-bomb").apply {
            category = Category.effect
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    CioItem.ic + 3,
                    Items.pyratite + 1,
                    Items.blastCompound + 2,
                    Items.coal + 3,
                )
                explosionRange = 10f * Vars.tilesize * size
                explosionDamage = 150f * Vars.tilesize * size
            }
            ErekirSpec {
                requirements = arrayOf(
                    CioItem.ic + 5,
                    Items.oxide + 5,
                )
                explosionRange = 10f * Vars.tilesize * size
                explosionDamage = 80f * Vars.tilesize * size
            }
            size = 2
            maxSensitive = 5
        }
    }
}