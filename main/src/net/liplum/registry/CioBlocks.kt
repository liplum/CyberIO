package net.liplum.registry

import arc.func.Prov
import arc.graphics.Color
import arc.graphics.Texture
import arc.math.Interp
import arc.util.Time
import mindustry.Vars
import mindustry.content.*
import mindustry.entities.bullet.BasicBulletType
import mindustry.entities.bullet.LaserBulletType
import mindustry.entities.bullet.LightningBulletType
import mindustry.entities.effect.MultiEffect
import mindustry.entities.part.DrawPart.PartProgress
import mindustry.entities.pattern.ShootAlternate
import mindustry.entities.pattern.ShootSpread
import mindustry.game.EventType.Trigger
import mindustry.gen.Sounds
import mindustry.graphics.Layer
import mindustry.type.Category
import mindustry.world.blocks.defense.turrets.ContinuousLiquidTurret
import mindustry.world.blocks.defense.turrets.PowerTurret.PowerTurretBuild
import mindustry.world.blocks.environment.Floor
import mindustry.world.blocks.heat.HeatProducer
import mindustry.world.blocks.payloads.*
import mindustry.world.blocks.production.GenericCrafter
import mindustry.world.blocks.production.HeatCrafter
import mindustry.world.blocks.sandbox.ItemSource
import mindustry.world.blocks.sandbox.LiquidSource
import mindustry.world.blocks.sandbox.PowerSource
import mindustry.world.draw.DrawLiquidTile
import mindustry.world.draw.DrawMulti
import mindustry.world.meta.BuildVisibility
import net.liplum.*
import net.liplum.annotations.DependOn
import net.liplum.annotations.Only
import net.liplum.annotations.Subscribe
import net.liplum.api.brain.UT
import net.liplum.api.brain.Upgrade
import net.liplum.api.bullets.MultiBulletAbility
import net.liplum.api.virus.setUninfected
import net.liplum.api.virus.setUninfectedFloor
import net.liplum.blocks.bomb.ZipBomb
import net.liplum.blocks.cyberion.DrawCyberionAgglomeration
import net.liplum.blocks.ddos.DDoS
import net.liplum.blocks.decentralizer.Decentralizer
import net.liplum.blocks.deleter.Deleter
import net.liplum.blocks.deleter.DeleterWave
import net.liplum.blocks.deleter.deleted
import net.liplum.blocks.ic.ICMachine
import net.liplum.blocks.ic.ICMachineSmall
import net.liplum.blocks.jammer.Jammer
import net.liplum.blocks.jammer.JammingLaser
import net.liplum.blocks.power.WirelessTower
import net.liplum.blocks.prism.Prism
import net.liplum.blocks.prism.PrismObelisk
import net.liplum.blocks.sandbox.AdjustableOverdrive
import net.liplum.blocks.stream.P2pNode
import net.liplum.blocks.stream.StreamClient
import net.liplum.blocks.stream.StreamHost
import net.liplum.blocks.stream.StreamServer
import net.liplum.blocks.tmtrainer.CharBulletType
import net.liplum.blocks.tmtrainer.RandomName
import net.liplum.blocks.tmtrainer.TMTRAINER
import net.liplum.blocks.tmtrainer.TMTRAINER.DrawCore
import net.liplum.blocks.underdrive.UnderdriveProjector
import net.liplum.blocks.virus.AntiVirus
import net.liplum.blocks.virus.Virus
import net.liplum.brain.*
import net.liplum.bullet.*
import net.liplum.common.shader.ShaderBase
import net.liplum.data.*
import net.liplum.holo.*
import plumy.core.arc.invoke
import plumy.core.math.smooth
import net.liplum.mdt.Else
import net.liplum.mdt.render.*
import net.liplum.mdt.ui.DynamicContentInfoDialog.Companion.registerDynamicInfo
import net.liplum.mdt.utils.addAmmo
import net.liplum.mdt.utils.plus
import net.liplum.registry.CioBulletTypes.optInRadiationInterference
import net.liplum.registry.CioBulletTypes.optInVirus
import net.liplum.render.*
import net.liplum.statusFx.StaticFx
import net.liplum.util.globalAnim

object CioBlocks {
    @JvmStatic lateinit var icMachine: GenericCrafter
    @JvmStatic lateinit var icAssembler: GenericCrafter
    @JvmStatic lateinit var icMachineSmall: GenericCrafter
    @JvmStatic lateinit var receiver: Receiver
    @JvmStatic lateinit var sender: Sender
    @JvmStatic lateinit var virus: Virus
    @JvmStatic lateinit var landProjector: LandProjector
    @JvmStatic lateinit var holoFloor: HoloFloor
    @JvmStatic lateinit var underdriveProjector: UnderdriveProjector
    @JvmStatic lateinit var antiVirus: AntiVirus
    @JvmStatic lateinit var prism: Prism
    @JvmStatic lateinit var prismObelisk: PrismObelisk
    @JvmStatic lateinit var deleter: Deleter
    @JvmStatic lateinit var hyperOverdriveSphere: AdjustableOverdrive
    @JvmStatic lateinit var holoWall: HoloWall
    @JvmStatic lateinit var holoWallLarge: HoloWall
    @JvmStatic lateinit var TMTRAINER: TMTRAINER
    @JvmStatic lateinit var smartDistributor: SmartDistributor
    @JvmStatic lateinit var smartUnloader: SmartUnloader
    @JvmStatic lateinit var streamClient: StreamClient
    @JvmStatic lateinit var streamHost: StreamHost
    @JvmStatic lateinit var streamServer: StreamServer
    @JvmStatic lateinit var jammer: Jammer
    @JvmStatic lateinit var cyberionMixer: GenericCrafter
    @JvmStatic lateinit var holoProjector: HoloProjector
    @JvmStatic lateinit var aquacyberion: Floor
    @JvmStatic lateinit var stealth: Stealth
    @JvmStatic lateinit var wirelessTower: WirelessTower
    @JvmStatic lateinit var heimdall: Heimdall
    @JvmStatic lateinit var eye: Eye
    @JvmStatic lateinit var ear: Ear
    @JvmStatic lateinit var heart: Heart
    @JvmStatic lateinit var decentralizer: Decentralizer
    @JvmStatic lateinit var DDoS: DDoS
    @JvmStatic lateinit var dataCDN: DataCDN
    @JvmStatic lateinit var zipBomb: ZipBomb
    @JvmStatic lateinit var serializer: Serializer
    @JvmStatic lateinit var p2pNode: P2pNode
    @JvmStatic lateinit var cuttex: ContinuousLiquidTurret
    @DependOn("CioItems.ic")
    fun icMachine() {
        icMachine = ICMachine("ic-machine").apply {
            category = Category.crafting
            buildVisibility = BuildVisibility.shown
            requirements = arrayOf(
                CioItems.ic + 2,
                Items.copper + 550,
                Items.lead + 280,
                Items.silicon + 150,
                Items.graphite + 250,
            )
            scaledHealth = 200f
            consumeItems(
                //Total:100
                Items.copper + 50,  //50%
                Items.silicon + 20,  //20%
                Items.metaglass + 30, //30%
            )
            consumePower(10f)
            craftTime = 400f
            fogRadius = 3
            outputItem = CioItems.ic + 2
            size = 3
            buildCostMultiplier = 1.5f
            craftEffect = Fx.smeltsmoke
            itemCapacity = 60
        }
    }
    @DependOn("CioItems.ic")
    fun icMachineSmall() {
        icMachineSmall = ICMachineSmall("ic-machine-s").apply {
            category = Category.crafting
            buildVisibility = BuildVisibility.hidden
            requirements = arrayOf(
                Items.copper + 150,
                Items.silicon + 50,
                Items.graphite + 50,
            )
            scaledHealth = 80f
            craftTime = 1150f
            itemCapacity = 40
            consumeItems(
                Items.copper + 25,
                Items.sand + 40,
                Items.lead + 15,
            )
            consumePower(2f)
            outputItem = CioItems.ic + 1
            fogRadius = 2
            size = 2
            craftEffect = Fx.formsmoke

            processIcons = arrayOf(
                Items.metaglass,
                Items.silicon,
                CioItems.ic
            )
        }
    }
    @DependOn("CioItems.ic")
    fun icAssembler() {
        icAssembler = GenericCrafter("ic-assembler").apply {
            category = Category.crafting
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    Items.lead + 60,
                    Items.graphite + 35,
                    Items.metaglass + 50,
                    Items.silicon + 50,
                )
                scaledHealth = 80f
                consumePower(1.2f)
                itemCapacity = 40
                consumeItems(
                    Items.copper + 20,
                    Items.silicon + 15,
                    Items.metaglass + 5,
                )
                craftTime = 600f
                drawer = DrawMulti(
                    DrawRegionSpec("-bottom"),
                    SpecDrawConstruct(stages = 3),
                    DrawDefaultSpec(),
                )
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
                drawer = DrawMulti(
                    DrawRegionSpec("-bottom"),
                    SpecDrawConstruct(stages = 4),
                    DrawDefaultSpec(),
                )
                squareSprite = false
            }
            buildType = Prov { GenericCrafterBuild() }
            fogRadius = 2
            size = 2
            outputItem = CioItems.ic + 1
            craftEffect = Fx.smeltsmoke
        }
    }
    @DependOn("CioItems.ic")
    fun receiver() {
        receiver = Receiver("receiver").apply {
            category = Category.distribution
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    CioItems.ic + 1,
                    Items.lead + 150,
                    Items.graphite + 120,
                    Items.metaglass + 60,
                    Items.silicon + 180,
                )
                scaledHealth = 140f
                maxConnection = 5
                consumePower(1.5f)
            }
            ErekirSpec {
                requirements = arrayOf(
                    CioItems.ic + 40,
                    Items.graphite + 80,
                    Items.tungsten + 30,
                    Items.silicon + 30,
                )
                scaledHealth = 250f
                maxConnection = 3
                consumePower(0.8f)
            }
            replaceable = false
        }
    }
    @DependOn("CioItems.ic")
    fun sender() {
        sender = Sender("sender").apply {
            category = Category.distribution
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    CioItems.ic + 1,
                    Items.lead + 150,
                    Items.graphite + 120,
                    Items.metaglass + 60,
                    Items.silicon + 180,
                )
                scaledHealth = 140f
                consumePower(1.5f)
            }
            ErekirSpec {
                requirements = arrayOf(
                    CioItems.ic + 40,
                    Items.graphite + 120,
                    Items.tungsten + 50,
                    Items.silicon + 50,
                )
                scaledHealth = 250f
                maxRange = 800f
                consumePower(1f)
            }
            replaceable = false
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
    @DependOn("CioItems.ic")
    fun landProjector() {
        landProjector = LandProjector("land-projector").apply {
            category = Category.effect
            DebugOnly {
                buildVisibility = BuildVisibility.shown
            }.Else {
                buildVisibility = BuildVisibility.hidden
            }
            requirements = arrayOf(
                CioItems.ic + 5,
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
    @DependOn("CioItems.ic")
    fun underdriveProjector() {
        underdriveProjector = UnderdriveProjector("underdrive-projector").apply {
            category = Category.power
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    CioItems.ic + 8,
                    Items.copper + 300,
                    Items.lead + 20,
                    Items.silicon + 240,
                    Items.plastanium + 10,
                    Items.phaseFabric + 5,
                )
                scaledHealth = 350f
                powerProduction = 4.5f
                maxPowerEFFBlocksReq = 22
                maxGear = 5
            }
            ErekirSpec {
                requirements = arrayOf(
                    CioItems.ic + 25,
                    Items.tungsten + 15,
                    Items.silicon + 50,
                    Items.beryllium + 80,
                )
                scaledHealth = 250f
                range = 45f
                powerProduction = 2f
                maxPowerEFFBlocksReq = 18
                maxGear = 8
            }
            color = R.C.LightBlue
            maxSlowDownRate = 0.9f
            size = 1
        }
    }
    @DependOn("CioItems.ic")
    fun antiVirus() {
        antiVirus = AntiVirus("anti-virus").apply {
            category = Category.effect
            buildVisibility = BuildVisibility.shown
            requirements = arrayOf(
                CioItems.ic + 1,
                Items.copper + 100,
                Items.graphite + 40,
                Items.silicon + 25
            )
            scaledHealth = 120f
            consumePower(0.5f)
            size = 1
        }.setUninfected()
    }
    @DependOn
    fun hyperOverdriveSphere() {
        hyperOverdriveSphere = AdjustableOverdrive("hyper-overdrive-sphere").apply {
            category = Category.effect
            DebugOnly {
                buildVisibility = BuildVisibility.shown
            }.Else {
                buildVisibility = BuildVisibility.sandboxOnly
            }
            requirements = emptyArray()
            size = 3
            maxBoost = 50f
            minBoost = 0.5f
            speedBoost = 50f
            range = 1000f
        }
    }
    @DependOn("CioItems.ic")
    fun prism() {
        prism = Prism("prism").apply {
            category = Category.turret
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    CioItems.ic + 15,
                    Items.copper + 250,
                    Items.metaglass + 350,
                    Items.titanium + 50,
                )
                scaledHealth = 150f
            }
            ErekirSpec {
                requirements = arrayOf(
                    CioItems.ic + 120,
                    Items.carbide + 100,
                    Items.beryllium + 250,
                    Items.tungsten + 250,
                )
                scaledHealth = 125f
            }
            buildCostMultiplier = 2f
            size = 4
        }
    }
    @DependOn(
        "CioItems.ic",
        "CioBlocks.prism"
    )
    fun prismObelisk() {
        prismObelisk = PrismObelisk("prism-obelisk").apply {
            category = Category.turret
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    CioItems.ic + 3,
                    Items.copper + 60,
                    Items.plastanium + 120,
                    Items.metaglass + 240,
                    Items.titanium + 10,
                )
                scaledHealth = 160f
            }
            ErekirSpec {
                requirements = arrayOf(
                    CioItems.ic + 45,
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
    @DependOn("CioItems.ic")
    fun deleter() {
        deleter = Deleter("deleter").apply {
            category = Category.turret
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    CioItems.ic + 9,
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
                    CioItems.ic + 75,
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
                        heatColor = S.Hologram
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
                        heatColor = S.Hologram
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
                then add DrawTurretHeat<PowerTurretBuild>("-glow") { warmup() }
            }
        }
    }
    @DependOn("CioItems.ic")
    fun holoWall() {
        holoWall = HoloWall("holo-wall").apply {
            category = Category.defense
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    CioItems.ic + 1,
                    Items.silicon + 6,
                    Items.titanium + 12,
                    Items.plastanium + 10,
                )
                scaledHealth = 1000f
                restoreCharge = 10 * 60f
            }
            ErekirSpec {
                requirements = arrayOf(
                    CioItems.ic + 30,
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
            lightColor = S.Hologram
            lightRadius = 40f
            floatingRange = 1f
            size = 1
            buildCostMultiplier = 3.5f
        }
    }
    @DependOn(
        "CioItems.ic",
        "CioBlocks.holoWall"
    )
    fun holoWallLarge() {
        holoWallLarge = HoloWall("holo-wall-large").apply {
            category = Category.defense
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    CioItems.ic + 2,
                    Items.beryllium + 80,
                    Items.titanium + 48,
                    Items.plastanium + 40,
                )
                restoreCharge = 15 * 60f
                scaledHealth = 820f
            }
            ErekirSpec {
                requirements = arrayOf(
                    CioItems.ic + 56,
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
            lightColor = S.Hologram
            lightRadius = 80f
            floatingRange = 2f
            squareSprite = false
            size = 2
            buildCostMultiplier = 4.5f
        }
    }
    @DependOn(
        "CioItems.ic",
        "CioSEffects.infected",
        "CioSEffects.static",
    )
    fun TMTRAINER() {
        TMTRAINER = TMTRAINER("TMTRAINER").apply {
            category = Category.turret
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    CioItems.ic + 5,
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
                shoot = ShootAlternate().apply {
                    spread = 4f
                }
                scaledHealth = 250f
            }
            ErekirSpec {
                requirements = arrayOf(
                    CioItems.ic + 35,
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
                addAmmo(CioItems.ic, CharBulletType().apply {
                    speed = 2.4f
                    damage = 150f
                    lifetime = 180f
                    hitSize = 5f
                    ammoMultiplier = 5f
                    reloadMultiplier = 0.8f
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
                then add DrawCore()
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
    @DependOn("CioItems.ic")
    fun smartDistributor() {
        smartDistributor = SmartDistributor("smart-distributor").apply {
            category = Category.distribution
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    CioItems.ic + 15,
                    Items.copper + 550,
                    Items.silicon + 210,
                    Items.plastanium + 80,
                    Items.thorium + 140,
                    Items.surgeAlloy + 50,
                )
                scaledHealth = 800f
                maxConnection = -1
            }
            ErekirSpec {
                requirements = arrayOf(
                    CioItems.ic + 75,
                    Items.tungsten + 200,
                    Items.carbide + 120,
                    Items.surgeAlloy + 50,
                )
                scaledHealth = 500f
                maxConnection = -1
            }
            size = 2
            ArrowsAnimFrames = 4
            ArrowsAnimDuration = 12f
        }
    }
    @DependOn("CioItems.ic")
    fun smartUnloader() {
        smartUnloader = SmartUnloader("smart-unloader").apply {
            category = Category.distribution
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    CioItems.ic + 5,
                    Items.lead + 350,
                    Items.silicon + 210,
                    Items.graphite + 150,
                    Items.titanium + 50,
                )
                unloadSpeed = 5f
                scaledHealth = 300f
                maxConnection = 5
            }
            ErekirSpec {
                requirements = arrayOf(
                    CioItems.ic + 200,
                    Items.tungsten + 350,
                    Items.carbide + 120,
                    Items.phaseFabric + 120,
                    Items.surgeAlloy + 50,
                )
                unloadSpeed = 6f
                scaledHealth = 1000f
                maxRange = 1500f
                maxConnection = 6
            }
            size = 2
            ShrinkingAnimFrames = 7
            ShrinkingAnimDuration = 25f
        }
    }
    @DependOn("CioItems.ic")
    fun streamClient() {
        streamClient = StreamClient("stream-client").apply {
            category = Category.liquid
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    CioItems.ic + 2,
                    Items.metaglass + 40,
                    Items.silicon + 20,
                    Items.graphite + 40,
                    Items.titanium + 10,
                )
                scaledHealth = 180f
                maxConnection = 5
                consumePower(1.8f)
                liquidCapacity = 200f
            }
            ErekirSpec {
                requirements = arrayOf(
                    CioItems.ic + 35,
                    Items.beryllium + 40,
                    Items.silicon + 20,
                    Items.tungsten + 40,
                )
                scaledHealth = 200f
                maxConnection = 3
                consumePower(1.5f)
                liquidCapacity = 80f
            }
            replaceable = false
            size = 1
        }
    }
    @DependOn("CioItems.ic")
    fun streamHost() {
        streamHost = StreamHost("stream-host").apply {
            category = Category.liquid
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    CioItems.ic + 5,
                    Items.lead + 100,
                    Items.metaglass + 600,
                    Items.silicon + 80,
                    Items.graphite + 60,
                    Items.titanium + 40,
                    Items.plastanium + 20,
                )
                scaledHealth = 500f
                powerUseBase = 2f
                powerUsePerConnection = 1f
                networkSpeed = 3.5f
                liquidCapacity = 800f
                maxConnection = 5
            }
            ErekirSpec {
                requirements = arrayOf(
                    CioItems.ic + 54,
                    Items.tungsten + 250,
                    Items.carbide + 100,
                    Items.beryllium + 400,
                )
                scaledHealth = 350f
                powerUseBase = 1.4f
                powerUsePerConnection = 2.5f
                networkSpeed = 5f
                liquidCapacity = 1200f
                maxConnection = 3
                maxRange = 1800f
            }
            size = 2
            replaceable = false
        }
    }
    @DependOn("CioItems.ic")
    fun streamServer() {
        streamServer = StreamServer("stream-server").apply {
            category = Category.liquid
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    CioItems.ic + 16,
                    Items.copper + 1200,
                    Items.lead + 400,
                    Items.metaglass + 1200,
                    Items.silicon + 320,
                    Items.thorium + 40,
                    Items.phaseFabric + 120,
                )
                scaledHealth = 500f
                networkSpeed = 15f
                researchCostMultiplier = 0.7f
                powerUseBase = 3.5f
                maxConnection = 5
                powerUsePerConnection = 2f
                liquidCapacity = 2000f
            }
            ErekirSpec {
                requirements = arrayOf(
                    CioItems.ic + 200,
                    Items.tungsten + 300,
                    Items.carbide + 150,
                    Items.beryllium + 500,
                    Items.phaseFabric + 120,
                )
                scaledHealth = 380f
                networkSpeed = 15f
                liquidCapacity = 3000f
                researchCostMultiplier = 0.6f
                powerUseBase = 2.5f
                maxConnection = 8
                maxRange = 2800f
                powerUsePerConnection = 3.8f
            }
            fireproof = true
            squareSprite = false
            size = 3
            replaceable = false
        }
    }
    @DependOn(
        "CioItems.ic",
        "CioFluids.cyberion",
    )
    fun jammer() {
        jammer = Jammer("jammer").apply {
            category = Category.turret
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    CioItems.ic + 8,
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
                    CioItems.ic + 80,
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
            chargeSound = CioSounds.jammerPreShoot
            shootSound = Sounds.none
            loopSound = CioSounds.tvStatic
            loopSoundVolume = 0.3f
            rotateSpeed = 2f

            addAmmo(CioFluids.cyberion, JammingLaser().apply {
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
                status = CioSEffects.static
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
                    }
                    regionPart("-side") {
                        mirror = true
                        progress = PartProgress.warmup
                        moveX = 4f
                        moveY = -4f
                    }
                }
                then add DrawStereo()
            }
        }
    }
    @DependOn(
        "CioItems.ic",
        "CioFluids.cyberion"
    )
    fun cyberionMixer() {
        VanillaSpec {
            cyberionMixer = HeatProducer("cyberion-mixer").apply {
                category = Category.crafting
                buildVisibility = BuildVisibility.shown
                requirements = arrayOf(
                    CioItems.ic + 4,
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
                outputLiquid = CioFluids.cyberion + 0.25f
                heatOutput = 3f
                drawer = DrawMulti(
                    DrawRegionSpec("-bottom"),
                    DrawLiquidTile(Liquids.cryofluid, 3f),
                    DrawLiquidTile(CioFluids.cyberion, 3f),
                    DrawDefaultSpec(),
                    DrawHeatOutputSpec().apply {
                        heatColor = S.Hologram
                    },
                    DrawCyberionAgglomeration(),
                )
                size = 3
            }
        }
        ErekirSpec {
            cyberionMixer = HeatCrafter("cyberion-mixer").apply {
                category = Category.crafting
                buildVisibility = BuildVisibility.shown
                requirements = arrayOf(
                    CioItems.ic + 30,
                    Items.carbide + 80,
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
                outputLiquid = CioFluids.cyberion + 0.25f
                drawer = DrawMulti(
                    DrawRegionSpec("-bottom"),
                    DrawPistonsSpec().apply {
                        sinMag = 3f
                        sinScl = 5f
                    },
                    DrawGlowRegionSpec(),
                    DrawDefaultSpec(),
                    DrawLiquidTile(Liquids.slag, 37f / 4f),
                    DrawLiquidTile(CioFluids.cyberion, 37f / 4f),
                    DrawRegionSpec("-top"),
                    DrawHeatInputSpec().apply {
                        heatColor = S.Hologram
                    },
                )
                size = 3
            }
        }
    }
    @DependOn(
        "CioItems.ic",
        "CioFluids.cyberion",
        "CioUnitTypes.holoMiner",
        "CioUnitTypes.holoFighter",
        "CioUnitTypes.holoGuardian",
        "CioUnitTypes.holoArchitect",
        "CioUnitTypes.holoSupporter",
    )
    fun holoProjector() {
        holoProjector = HoloProjector("holo-projector").apply {
            category = Category.units
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    CioItems.ic + 16,
                    Items.silicon + 220,
                    Items.graphite + 300,
                    Items.metaglass + 500,
                    Items.thorium + 1200,
                )
                liquidCapacity = 100f
                scaledHealth = 100f
                researchCostMultiplier = 0.8f
                plans += HoloPlan(
                    CioUnitTypes.holoMiner,
                    Requirement(
                        cyberion = 3.0f / 60f
                    ),
                    15f * 60f
                )

                plans += HoloPlan(
                    CioUnitTypes.holoFighter,
                    Requirement(
                        cyberion = 3.0f / 60f
                    ),
                    15f * 60f
                )
                plans += HoloPlan(
                    CioUnitTypes.holoGuardian,
                    Requirement(
                        cyberion = 1.5f / 60f
                    ),
                    7.5f * 60f
                )
                plans += HoloPlan(
                    CioUnitTypes.holoArchitect,
                    Requirement(
                        cyberion = 6.0f / 60f
                    ),
                    25f * 60f
                )
                plans += HoloPlan(
                    CioUnitTypes.holoSupporter,
                    Requirement(
                        cyberion = 2.5f / 60f
                    ),
                    12f * 60f
                )
            }
            ErekirSpec {
                liquidCapacity = 100f
                requirements = arrayOf(
                    CioItems.ic + 100,
                    Items.oxide + 220,
                    Items.thorium + 300,
                    Items.carbide + 120,
                    Items.silicon + 800,
                )
                scaledHealth = 150f
                researchCostMultiplier = 0.75f
                plans += HoloPlan(
                    CioUnitTypes.holoMiner,
                    Requirement(
                        items = arrayOf(CioItems.ic + 1),
                        cyberion = 1.5f / 60f
                    ),
                    18f * 60f
                )
                plans += HoloPlan(
                    CioUnitTypes.holoFighter,
                    Requirement(
                        items = arrayOf(CioItems.ic + 2),
                        cyberion = 1.5f / 60f
                    ),
                    15f * 60f
                )
                plans += HoloPlan(
                    CioUnitTypes.holoGuardian,
                    Requirement(
                        items = arrayOf(CioItems.ic + 1),
                        cyberion = 1.0f / 60f
                    ),
                    7.5f * 60f
                )
                plans += HoloPlan(
                    CioUnitTypes.holoArchitect,
                    Requirement(
                        items = arrayOf(CioItems.ic + 2),
                        cyberion = 3.0f / 60f
                    ),
                    25f * 60f
                )
                plans += HoloPlan(
                    CioUnitTypes.holoSupporter,
                    Requirement(
                        items = arrayOf(CioItems.ic + 3),
                        cyberion = 6f / 60f
                    ),
                    18f * 60f
                )
            }
            ambientSound = Sounds.build
            squareSprite = false
            size = 5
            buildCostMultiplier = 2f
        }
    }
    @DependOn("CioFluids.cyberion")
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
            liquidDrop = CioFluids.cyberion
            liquidMultiplier = 0.1f
            isLiquid = true
            cacheLayer = CioCLs.cyberion
            emitLight = true
            lightRadius = 30f
            lightColor = S.Hologram.cpy().a(0.19f)
        }
    }
    @DependOn(
        "CioItems.ic",
        "CioFluids.cyberion"
    )
    fun stealth() {
        stealth = Stealth("stealth").apply {
            category = Category.turret
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    CioItems.ic + 2,
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
                    CioItems.ic + 35,
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

            shootType = RuvikBullet().apply bullet@{
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
                trailColor = S.Hologram
            }
        }
    }
    @DependOn("CioItems.ic")
    fun wirelessTower() {
        wirelessTower = WirelessTower("wireless-tower").apply {
            category = Category.power
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    CioItems.ic + 2,
                    Items.copper + 310,
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
                    CioItems.ic + 50,
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
    @DependOn("CioItems.ic")
    fun heimdall() {
        heimdall = Heimdall("heimdall").apply {
            category = Category.turret
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    CioItems.ic + 10,
                    Items.sporePod + 300,
                    Items.thorium + 150,
                    Items.metaglass + 50,
                    Items.copper + 120,
                    Items.silicon + 180,
                    Items.plastanium + 50,
                )
                scaledHealth = 400f
                range = 175f
                powerUse = 2.5f
                damage = 8f
            }
            ErekirSpec {
                requirements = arrayOf(
                    CioItems.ic + 50,
                    Items.oxide + 80,
                    Items.carbide + 40,
                    Items.thorium + 150,
                    Items.graphite + 150,
                )
                scaledHealth = 350f
                range = 145f
                powerUse = 2.4f
                waveSpeed = 1.8f
                damage = 10f
                reloadTime = 75f
            }
            loopSound = Sounds.minebeam
            size = 4
            connectedSound = CioSounds.connected
            addFormationPatterns(
                FaceFE, FunnyFaceFE, ForceFieldFE
            )
        }
    }
    @DependOn("CioItems.ic")
    fun eye() {
        eye = Eye("heimdall-eye").apply {
            category = Category.turret
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    CioItems.ic + 2,
                    Items.pyratite + 15,
                    Items.plastanium + 10,
                    Items.metaglass + 40,
                    Items.copper + 60,
                    Items.silicon + 30,
                )
                range = 165f
                scaledHealth = 300f
                consumePower(3f)
            }
            ErekirSpec {
                requirements = arrayOf(
                    CioItems.ic + 30,
                    Items.carbide + 25,
                    Items.tungsten + 40,
                    Items.silicon + 60,
                )
                range = 175f
                scaledHealth = 250f
                consumePower(2.4f)
            }
            shoot.firstShotDelay = 60f
            size = 2
            moveWhileCharging = false
            shootEffect = BrainFx.eyeShoot
            smokeEffect = Fx.none
            addUpgrade(
                Upgrade(UT.Damage, false, 0.05f),
                Upgrade(UT.ReloadTime, true, -4.5f),
                Upgrade(UT.ControlLine, true, 0.01f),
                Upgrade(UT.ForceFieldRegen, false, 0.3f),
                Upgrade(UT.Range, false, -0.05f),
                Upgrade(UT.ForceFieldRadius, true, -3f),
                Upgrade(UT.WaveWidth, true, -0.1f),
                Upgrade(UT.PowerUse, false, 0.55f),
                Upgrade(UT.MaxBrainWaveNum, true, 0.2f),
            )
            normalSounds = CioSounds.laserWeak
            improvedSounds = CioSounds.laser
            soundVolume = 0.15f
            normalBullet = LightningBulletType().apply {
                VanillaSpec {
                    damage = 90f
                }
                ErekirSpec {
                    damage = 240f
                }
                lightningLength = 25
                collidesAir = false
                ammoMultiplier = 1f
                recoil = 3f
                shootCone = 3f
                accurateDelay = true
                lightningColor = R.C.RedAlert
            }
            improvedBullet = LaserBulletType().apply {
                VanillaSpec {
                    damage = 250f
                }
                ErekirSpec {
                    damage = 550f
                }
                colors = arrayOf(R.C.RedAlert.cpy().a(0.4f), R.C.RedAlert, R.C.RedAlertDark)
                lightningColor = R.C.RedAlertDark
                chargeEffect = MultiEffect(BrainFx.eyeCharge, BrainFx.eyeChargeBegin)
                hitEffect = Fx.hitLancer
                hitSize = 4f
                lifetime = 16f
                recoil = 4f
                drawSize = 200f
                shootCone = 3f
                length = 173f
                accurateDelay = true
                ammoMultiplier = 1f
            }
        }
    }
    @DependOn("CioItems.ic")
    fun ear() {
        ear = Ear("heimdall-ear").apply {
            category = Category.turret
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    CioItems.ic + 2,
                    Items.sporePod + 20,
                    Items.graphite + 5,
                    Items.copper + 60,
                    Items.silicon + 50,
                    Items.plastanium + 10,
                )
                range = 145f
                scaledHealth = 300f
                damage = 4f
            }
            ErekirSpec {
                requirements = arrayOf(
                    CioItems.ic + 30,
                    Items.graphite + 50,
                    Items.beryllium + 60,
                    Items.silicon + 50,
                    Items.phaseFabric + 20,
                )
                range = 165f
                scaledHealth = 250f
                damage = 8f
                waveSpeed = 2.2f
            }
            loopSound = Sounds.wind
            addUpgrade(
                Upgrade(UT.Damage, false, -0.02f),
                Upgrade(UT.Range, false, 0.1f),
                Upgrade(UT.WaveSpeed, true, 0.08f),
                Upgrade(UT.WaveWidth, true, 0.4f),
                Upgrade(UT.ForceFieldRadius, true, 5f),
                Upgrade(UT.ForceFieldMax, false, 0.2f),
                Upgrade(UT.PowerUse, false, 0.35f),
                Upgrade(UT.MaxBrainWaveNum, true, 0.15f),
            )
            buildType = Prov { EarBuild() }
            size = 2
        }
    }
    @DependOn("CioItems.ic")
    fun heart() {
        DebugOnly {
            heart = Heart("heimdall-heart").apply {
                category = Category.turret
                buildVisibility = BuildVisibility.shown
                VanillaSpec {
                    requirements = arrayOf(
                        CioItems.ic + 4,
                        Items.graphite + 200,
                        Items.metaglass + 500,
                        Items.silicon + 50,
                        Items.blastCompound + 200,
                    )
                    scaledHealth = 125f
                    convertSpeed = 6f
                }
                ErekirSpec {
                    requirements = arrayOf(
                        CioItems.ic + 60,
                        Items.oxide + 100,
                        Items.tungsten + 500,
                        Items.silicon + 200,
                    )
                    convertSpeed = 7f
                    scaledHealth = 105f
                }
                size = 4
                blood = Blood()
                heartbeat.apply {
                    shake.config {
                        base = 1.5f
                        upRange = 4.8f - base
                        downRange = 0.9f
                    }
                    reloadTime.config {
                        // Decrease
                        base = 120f
                        upRange = 50f
                        downRange = 80f
                    }
                    powerUse.config {
                        base = 2f
                        upRange = 5f - base
                        downRange = 0f
                    }
                    damage.config {
                        base = 60f
                        upRange = 120f - base
                        downRange = 20f
                    }
                    range.config {
                        base = 165f
                        upRange = 240f - base
                        downRange = 0f
                    }
                    shootNumber.config {
                        base = 22
                        upRange = 34 - 22
                        downRange = 22 - 12
                    }
                    bloodCost.config {
                        base = 50f
                        upRange = 150f - 50f
                        downRange = 0f
                    }
                    systole.config {
                        base = 0.175f
                        upRange = 0.192f - 0.175f
                        downRange = 0.175f - 0.17f
                    }
                    diastole.config {
                        // Decrease
                        base = 3.3f
                        upRange = 3.5f - 3.3f
                        downRange = 3.3f - 3.15f
                    }
                    bulletLifeTime.config {
                        base = 200f
                        upRange = 300f - 200f
                        downRange = 50f
                    }
                    soundGetter = {
                        when (it) {
                            in Float.MIN_VALUE..0.1f -> CioSounds.heartbeat
                            in 0.1f..Float.MAX_VALUE -> CioSounds.heartbeatFaster
                            else -> CioSounds.heartbeat
                        }
                    }
                    offset = 20f // +5f when improved
                }
                bulletType = BBulletType("blood-bullet".cio).apply {
                    damage = 0f
                    lifetime = 0f
                    hitEffect = Fx.none
                    shootEffect = Fx.none
                    smokeEffect = Fx.none
                    layer = Layer.bullet - 0.1f
                    despawnEffect = BrainFx.bloodBulletHit
                    hitEffect = BrainFx.bloodBulletHit
                    collidesTiles = false
                    filter = Texture.TextureFilter.nearest
                    scale = { 2.4f + it.damage / 80f }
                    hitSize = 20f
                }
            }
        }
    }

    fun decentralizer() {
        DebugOnly {
            decentralizer = Decentralizer("decentralizer").apply {
                requirements(
                    Category.crafting, BuildVisibility.shown, arrayOf()
                )
                size = 4
            }
        }
    }
    @DependOn("CioItems.ic")
    fun DDos() {
        DebugOnly {
            DDoS = DDoS("DDoS").apply {
                category = Category.turret
                buildVisibility = BuildVisibility.shown
                requirements = arrayOf()
                maxDamage = 120f
                size = 4
                hitSizer = { damage / 80f * 4f }
                bulletType = AbilityItemBulletType().apply bullet@{
                    ability = MultiBulletAbility(
                        ProvidenceBA(),
                        SlowDownBA(),
                        InfiniteBA(),
                        //TeleportBA(),
                        BlackHoleBA(),
                        RestrictedAreaBA(),
                    ).apply {
                        bulletType = this@bullet
                    }
                    speed = 2f
                    damage = 0f
                    hitSize = 10f
                    pierce = true
                    pierceCap = 5
                    drawSizer = { damage / 80f }
                    trailLength = 8
                    lifetime = 180f
                    trailWidth = 4f
                }
                DebugOnly {
                    health = Int.MAX_VALUE
                }
            }
        }
    }
    @DependOn("CioItems.ic")
    fun dataCDN() {
        DebugOnly {
            dataCDN = DataCDN("data-cdn").apply {
                category = Category.units
                buildVisibility = BuildVisibility.shown
                requirements = arrayOf()
                size = 3
            }
        }
    }
    @DependOn("CioItems.ic")
    fun zipBomb() {
        zipBomb = ZipBomb("zip-bomb").apply {
            category = Category.effect
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    CioItems.ic + 1,
                    Items.blastCompound + 2,
                    Items.pyratite + 5,
                    Items.coal + 10,
                )
                damagePreUnit = 100f
            }
            ErekirSpec {
                requirements = arrayOf(
                    CioItems.ic + 5,
                    Items.oxide + 5,
                )
                damagePreUnit = 80f
            }
            size = 2
            maxSensitive = 5
        }
    }
    @DependOn("CioItems.ic")
    fun serializer() {
        DebugOnly {
            serializer = Serializer("serializer").apply {
                category = Category.effect
                buildVisibility = BuildVisibility.shown
                requirements = arrayOf()
                size = 5
                linkRange = 30f * Vars.tilesize
            }
        }
    }
    @DependOn("CioItems.ic")
    fun p2pNode() {
        p2pNode = P2pNode("p2p-node").apply {
            category = Category.liquid
            buildVisibility = BuildVisibility.shown
            VanillaSpec {
                requirements = arrayOf(
                    CioItems.ic + 2,
                    Items.lead + 40,
                    Items.metaglass + 50,
                    Items.silicon + 50,
                )
                scaledHealth = 120f
                liquidCapacity = 800f
                balancingSpeed = 0.5f
                maxRange = -1f
            }
            ErekirSpec {
                requirements = arrayOf(
                    CioItems.ic + 35,
                    Items.tungsten + 40,
                    Items.beryllium + 240,
                    Items.silicon + 180,
                )
                scaledHealth = 100f
                liquidCapacity = 600f
                balancingSpeed = 0.3f
                maxRange = 1450f
            }
            liquidPadding = 2f
            size = 2
            squareSprite = false
            consumePower(1.5f)
        }
    }
    @DependOn(
        "CioItems.ic",
        "CioFluids.cyberion"
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
    @DependOn
    fun _overwriteVanilla() {
        DebugOnly {
            (Blocks.powerSource as PowerSource).apply {
                buildVisibility = BuildVisibility.shown
                health = Int.MAX_VALUE
            }
            (Blocks.itemSource as ItemSource).apply {
                buildVisibility = BuildVisibility.shown
                health = Int.MAX_VALUE
            }
            (Blocks.liquidSource as LiquidSource).apply {
                buildVisibility = BuildVisibility.shown
                health = Int.MAX_VALUE
            }
            (Blocks.payloadSource as PayloadSource).apply {
                buildVisibility = BuildVisibility.shown
                health = Int.MAX_VALUE
            }
            Blocks.powerVoid.buildVisibility = BuildVisibility.shown
            Blocks.itemVoid.buildVisibility = BuildVisibility.shown
            Blocks.liquidVoid.buildVisibility = BuildVisibility.shown
            Blocks.payloadVoid.buildVisibility = BuildVisibility.shown
            val blockSize = 10f
            (Blocks.payloadConveyor as PayloadConveyor).payloadLimit = blockSize
            (Blocks.payloadLoader as PayloadLoader).maxBlockSize = blockSize.toInt()
            (Blocks.payloadRouter as PayloadRouter).payloadLimit = blockSize
            (Blocks.payloadUnloader as PayloadUnloader).maxBlockSize = blockSize.toInt()
            (Blocks.payloadPropulsionTower as PayloadMassDriver).maxPayloadSize = blockSize
            (Blocks.payloadMassDriver as PayloadMassDriver).maxPayloadSize = blockSize
            (Blocks.reinforcedPayloadConveyor as PayloadConveyor).payloadLimit = blockSize
            (Blocks.reinforcedPayloadRouter as PayloadConveyor).payloadLimit = blockSize
            UnitTypes.evoke.payloadCapacity = blockSize * blockSize * Vars.tilePayload
            UnitTypes.incite.payloadCapacity = blockSize * blockSize * Vars.tilePayload
            UnitTypes.emanate.payloadCapacity = blockSize * blockSize * Vars.tilePayload
            /*val coreBlock = Blocks.coreShard as CoreBlock
            coreBlock.unitType = CioUnitTypes.holoFighter
            coreBlock.solid = false*/
            Liquids.neoplasm.hidden = false
        }
        ExperimentalOnly {
            Blocks.conveyor.sync = true
            Blocks.titaniumConveyor.sync = true
            Blocks.armoredConveyor.sync = true
            Blocks.plastaniumConveyor.sync = true
        }
    }
}