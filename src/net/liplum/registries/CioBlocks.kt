package net.liplum.registries

import arc.Events
import arc.graphics.Color
import arc.struct.Seq
import mindustry.content.*
import mindustry.entities.bullet.LaserBulletType
import mindustry.game.EventType.Trigger
import mindustry.gen.Sounds
import mindustry.graphics.Pal
import mindustry.type.Category
import mindustry.type.ItemStack
import mindustry.type.LiquidStack
import mindustry.world.blocks.defense.OverdriveProjector
import mindustry.world.blocks.distribution.PayloadConveyor
import mindustry.world.blocks.distribution.PayloadRouter
import mindustry.world.blocks.environment.Floor
import mindustry.world.blocks.payloads.PayloadLoader
import mindustry.world.blocks.payloads.PayloadMassDriver
import mindustry.world.blocks.payloads.PayloadUnloader
import mindustry.world.blocks.production.GenericCrafter
import mindustry.world.blocks.production.LiquidConverter
import mindustry.world.consumers.ConsumePower
import mindustry.world.meta.BuildVisibility
import net.liplum.*
import net.liplum.api.virus.setUninfected
import net.liplum.api.virus.setUninfectedFloor
import net.liplum.blocks.cloud.Cloud
import net.liplum.blocks.cyberion.CyberionMixerDrawer
import net.liplum.blocks.debugonly.AdjustableOverdrive
import net.liplum.blocks.deleter.Deleter
import net.liplum.blocks.gadgets.SmartDistributor
import net.liplum.blocks.gadgets.SmartUnloader
import net.liplum.blocks.icmachine.ICMachine
import net.liplum.blocks.icmachine.ICMachineS
import net.liplum.blocks.jammer.Jammer
import net.liplum.blocks.power.WirelessTower
import net.liplum.blocks.prism.Prism
import net.liplum.blocks.prism.PrismObelisk
import net.liplum.blocks.rs.Receiver
import net.liplum.blocks.rs.Sender
import net.liplum.blocks.stream.StreamClient
import net.liplum.blocks.stream.StreamHost
import net.liplum.blocks.stream.StreamServer
import net.liplum.blocks.tmtrainer.RandomName
import net.liplum.blocks.tmtrainer.TMTRAINER
import net.liplum.blocks.underdrive.UnderdriveProjector
import net.liplum.blocks.virus.AntiVirus
import net.liplum.blocks.virus.Virus
import net.liplum.brains.Eye
import net.liplum.brains.Heimdall
import net.liplum.bullets.RuvikBullet
import net.liplum.bullets.STEM_VERSION
import net.liplum.bullets.ShaderCLaser
import net.liplum.holo.*
import net.liplum.lib.animations.ganim.globalAnim
import net.liplum.lib.shaders.SD
import net.liplum.seffects.StaticFx
import net.liplum.utils.otherConsumersAreValid

object CioBlocks : ContentTable {
    @JvmStatic lateinit var icMachine: GenericCrafter
    @JvmStatic lateinit var icMachineSmall: GenericCrafter
    @JvmStatic lateinit var receiver: Receiver
    @JvmStatic lateinit var sender: Sender
    @JvmStatic lateinit var virus: Virus
    @JvmStatic lateinit var landProjector: LandProjector
    @JvmStatic lateinit var holoFloor: HoloFloor
    @JvmStatic lateinit var underdriveProjector: UnderdriveProjector
    @JvmStatic lateinit var antiVirus: AntiVirus
    @JvmStatic lateinit var cloud: Cloud
    @JvmStatic lateinit var prism: Prism
    @JvmStatic lateinit var prismObelisk: PrismObelisk
    @JvmStatic lateinit var deleter: Deleter
    @JvmStatic lateinit var hyperOverdriveSphere: OverdriveProjector
    @JvmStatic lateinit var holoWall: HoloWall
    @JvmStatic lateinit var holoWallLarge: HoloWall
    @JvmStatic lateinit var TMTRAINER: TMTRAINER
    @JvmStatic lateinit var smartDistributor: SmartDistributor
    @JvmStatic lateinit var smartUnloader: SmartUnloader
    @JvmStatic lateinit var streamClient: StreamClient
    @JvmStatic lateinit var streamHost: StreamHost
    @JvmStatic lateinit var streamServer: StreamServer
    @JvmStatic lateinit var jammer: Jammer
    @JvmStatic lateinit var cyberionMixer: LiquidConverter
    @JvmStatic lateinit var holoProjector: HoloProjector
    @JvmStatic lateinit var aquacyberion: Floor
    @JvmStatic lateinit var stealth: Stealth
    @JvmStatic lateinit var wirelessTower: WirelessTower
    @JvmStatic lateinit var heimdall: Heimdall
    @JvmStatic lateinit var eye: Eye
    override fun firstLoad() {
    }

    override fun load() {
        icMachine = ICMachine("ic-machine").apply {
            requirements(
                Category.crafting, arrayOf(
                    ItemStack(Items.copper, 500),
                    ItemStack(Items.silicon, 100),
                    ItemStack(Items.graphite, 100),
                    ItemStack(Items.titanium, 250)
                )
            )
            health = 2000
            outputItem = ItemStack(CioItems.ic, 1)
            craftTime = 450f
            size = 3
            buildCostMultiplier = 3f
            craftEffect = Fx.smelt
            itemCapacity = 200
            consumes.items( //Total:200
                ItemStack(Items.silicon, 40),  //20%
                ItemStack(Items.copper, 100),  //50%
                ItemStack(Items.metaglass, 60) //30%
            )
            consumes.power(10f)
        }

        icMachineSmall = ICMachineS("ic-machine-s").apply {
            requirements(
                Category.crafting, arrayOf(
                    ItemStack(Items.copper, 100),
                    ItemStack(Items.silicon, 10),
                    ItemStack(Items.graphite, 20),
                )
            )
            health = 600
            outputItem = ItemStack(CioItems.ic, 1)
            craftTime = 1150f
            size = 2
            craftEffect = Fx.formsmoke
            itemCapacity = 40
            consumes.items(
                ItemStack(Items.copper, 30),
                ItemStack(Items.sand, 40),
                ItemStack(Items.lead, 10),
            )
            consumes.power(2f)

            processIcons = arrayOf(
                Items.metaglass,
                Items.silicon,
                CioItems.ic
            )
        }

        receiver = Receiver("receiver").apply {
            requirements(
                Category.distribution, arrayOf(
                    ItemStack(CioItems.ic, 1),
                    ItemStack(Items.copper, 50),
                    ItemStack(Items.graphite, 20),
                    ItemStack(Items.metaglass, 20),
                    ItemStack(Items.silicon, 10)
                )
            )
            health = 100
            consumes.power(0.5f)
            replaceable = false
        }

        sender = Sender("sender").apply {
            requirements(
                Category.distribution, arrayOf(
                    ItemStack(CioItems.ic, 1),
                    ItemStack(Items.copper, 50),
                    ItemStack(Items.graphite, 20),
                    ItemStack(Items.metaglass, 20),
                    ItemStack(Items.thorium, 10),
                )
            )
            health = 100
            consumes.power(0.5f)
            replaceable = false
        }

        virus = Virus("virus").apply {
            requirements(
                Category.logic, BuildVisibility.sandboxOnly,
                arrayOf(
                    ItemStack(Items.sporePod, 50),
                    ItemStack(Items.pyratite, 20),
                )
            )
            buildCostMultiplier = 5f
            spreadingSpeed = 200
            maxReproductionScale = 20
            maxGeneration = 100
            inheritChildrenNumber = false
            mutationRate = 10
        }.globalAnim(30f, 3)

        landProjector = LandProjector("land-projector").apply {
            requirements(
                Category.logic, BuildVisibility.sandboxOnly, arrayOf(
                    ItemStack(CioItems.ic, 5),
                    ItemStack(Items.graphite, 80),
                    ItemStack(Items.thorium, 100),
                    ItemStack(Items.silicon, 50)
                )
            )
            health = 100
            size = 2
            buildCostMultiplier = 3f
        }

        holoFloor = HoloFloor("holo-floor").apply {
            variants = 3
        }.setUninfectedFloor()

        underdriveProjector = UnderdriveProjector("underdrive-projector").apply {
            requirements(
                Category.effect, BuildVisibility.shown, arrayOf(
                    ItemStack(CioItems.ic, 1),
                    ItemStack(Items.copper, 120),
                    ItemStack(Items.coal, 80),
                    ItemStack(Items.metaglass, 75),
                )
            )
            health = 300
            color = R.C.LightBlue
            maxSlowDownRate = 0.9f
            powerProduction = 4.5f
            maxPowerEFFUnBlocksReq = 22
            maxGear = 5
            size = 1
        }

        antiVirus = AntiVirus("anti-virus").apply {
            requirements(
                Category.logic, BuildVisibility.shown, arrayOf(
                    ItemStack(CioItems.ic, 2),
                    ItemStack(Items.copper, 100),
                    ItemStack(Items.graphite, 40),
                    ItemStack(Items.silicon, 25)
                )
            )
            health = 500
            consumes.power(0.5f)
            size = 1
        }.setUninfected()

        cloud = Cloud("cloud").apply {
            requirements(
                Category.logic,
                if (CioMod.DebugMode)
                    BuildVisibility.sandboxOnly
                else
                    BuildVisibility.hidden, arrayOf(
                    ItemStack(CioItems.ic, 10),
                    ItemStack(Items.titanium, 1000),
                    ItemStack(Items.thorium, 1000),
                )
            )
            size = 3
            buildCostMultiplier = 2f
            health = 500 * size * size
            consumes.power(1f)
        }

        hyperOverdriveSphere = AdjustableOverdrive("hyper-overdrive-sphere").apply {
            requirements(
                Category.effect, BuildVisibility.sandboxOnly, arrayOf()
            )
            DebugOnly {
                buildVisibility = BuildVisibility.shown
            }
            size = 3
            maxBoost = 50f
            minBoost = 0.5f
            consumes.power(50f)
            speedBoost = 50f
            range = 1000f
        }

        prism = Prism("prism").apply {
            requirements(
                Category.turret, BuildVisibility.shown, arrayOf(
                    ItemStack(CioItems.ic, 7),
                    ItemStack(Items.metaglass, 100),
                    ItemStack(Items.titanium, 200),
                )
            )
            buildCostMultiplier = 2f
            size = 4
            health = 5000
        }

        prismObelisk = PrismObelisk("prism-obelisk").apply {
            requirements(
                Category.turret, BuildVisibility.shown, arrayOf(
                    ItemStack(CioItems.ic, 3),
                    ItemStack(Items.metaglass, 300),
                    ItemStack(Items.titanium, 300),
                )
            )
            size = 2
            health = 2000
            prismType = prism
        }

        deleter = Deleter("deleter").apply {
            requirements(
                Category.turret, BuildVisibility.shown, arrayOf(
                    ItemStack(CioItems.ic, 10),
                    ItemStack(Items.graphite, 100),
                    ItemStack(Items.silicon, 60),
                    ItemStack(Items.thorium, 250),
                )
            )
            range = 180f
            cooldown = 0.1f
            recoilAmount = 5f
            reloadTime = 15f
            powerUse = 3f
            size = 3
            buildCostMultiplier = 1.5f
            extraLostHpBounce = 0.005f
            health = 300 * size * size
            shootSound = Sounds.lasershoot
            configBullet {
                damage = 0.5f
            }
        }

        holoWall = HoloWall("holo-wall").apply {
            requirements(
                Category.defense, BuildVisibility.shown, arrayOf(
                    ItemStack(CioItems.ic, 1),
                    ItemStack(Items.titanium, 20),
                )
            )
            size = 1
            restoreReload = 10 * 60f
            health = 600
            buildCostMultiplier = 3.5f
        }

        holoWallLarge = HoloWall("holo-wall-large").apply {
            requirements(
                Category.defense, BuildVisibility.shown, arrayOf(
                    ItemStack(CioItems.ic, 2),
                    ItemStack(Items.titanium, 25 * 4),
                    ItemStack(Items.silicon, 10),
                )
            )
            size = 2
            restoreReload = 15 * 60f
            health = 550 * 5
            buildCostMultiplier = 4.5f
        }

        TMTRAINER = TMTRAINER("TMTRAINER").apply {
            requirements(
                Category.turret, BuildVisibility.shown, arrayOf(
                    ItemStack(CioItems.ic, 5),
                    ItemStack(Items.titanium, 100),
                    ItemStack(Items.graphite, 100),
                    ItemStack(Items.silicon, 50),
                )
            )
            ammo(
                Items.sporePod, CioBulletTypes.virus,
                Items.thorium, CioBulletTypes.radiationInterference,
            )
            maxAmmo = 80
            spread = 4f
            reloadTime = 5f
            restitution = 0.03f
            range = 260f
            shootCone = 15f
            shots = 2
            size = 4
            health = 250 * size * size
            limitRange(20f)
            ClientOnly {
                Events.run(Trigger.preDraw) {
                    WhenRefresh {
                        TMTRAINER.localizedName = RandomName.one(8)
                        TMTRAINER.description = RandomName.one(25)
                    }
                }
            }
        }
        smartDistributor = SmartDistributor("smart-distributor").apply {
            requirements(
                Category.distribution, BuildVisibility.shown, arrayOf(
                    ItemStack(CioItems.ic, 15),
                    ItemStack(Items.surgeAlloy, 50),
                    ItemStack(Items.thorium, 300),
                    ItemStack(Items.phaseFabric, 100),
                )
            )
            health = 1000
            size = 2
        }

        smartUnloader = SmartUnloader("smart-unloader").apply {
            requirements(
                Category.distribution, BuildVisibility.shown, arrayOf(
                    ItemStack(CioItems.ic, 8),
                    ItemStack(Items.metaglass, 80),
                    ItemStack(Items.silicon, 150),
                    ItemStack(Items.phaseFabric, 80),
                )
            )
            unloadSpeed = 5f
            health = 1000
            size = 2
        }

        streamClient = StreamClient("stream-client").apply {
            requirements(
                Category.liquid, BuildVisibility.shown, arrayOf(
                    ItemStack(CioItems.ic, 2),
                    ItemStack(Items.metaglass, 40),
                    ItemStack(Items.silicon, 10),
                    ItemStack(Items.titanium, 60),
                )
            )
            health = 150
            consumes.power(0.7f)
            liquidCapacity = 300f
            replaceable = false
        }

        streamHost = StreamHost("stream-host").apply {
            requirements(
                Category.liquid, BuildVisibility.shown, arrayOf(
                    ItemStack(CioItems.ic, 4),
                    ItemStack(Items.metaglass, 150),
                    ItemStack(Items.silicon, 35),
                    ItemStack(Items.titanium, 100),
                )
            )
            health = 400
            size = 2
            consumes.power(0.7f)
            networkSpeed = 2.5f
            liquidCapacity = 800f
            replaceable = false
        }

        streamServer = StreamServer("stream-server").apply {
            requirements(
                Category.liquid, BuildVisibility.shown, arrayOf(
                    ItemStack(CioItems.ic, 12),
                    ItemStack(Items.metaglass, 350),
                    ItemStack(Items.silicon, 100),
                    ItemStack(Items.thorium, 500),
                )
            )
            health = 2000
            size = 3
            consumes.power(2f)
            networkSpeed = 7f
            liquidCapacity = 2000f
            replaceable = false
        }

        jammer = Jammer("jammer").apply {
            requirements(
                Category.turret, BuildVisibility.shown, arrayOf(
                    ItemStack(CioItems.ic, 10),
                    ItemStack(Items.lead, 350),
                    ItemStack(Items.thorium, 200),
                    ItemStack(Items.surgeAlloy, 200),
                )
            )
            size = 3

            shootEffect = StaticFx
            shootCone = 40f
            recoilAmount = 4f
            shootShake = 2f
            range = 150f
            cooldown = 10f
            reloadTime = 40f
            firingMoveFract = 0.25f
            shootDuration = 180f
            shootSound = CioSounds.jammerPreShoot
            loopSound = CioSounds.tvStatic
            loopSoundVolume = 0.5f
            rotateSpeed = 2f
            powerUse = 15f

            shootType = ShaderCLaser().apply {
                damage = 120f
                length = range
                hitEffect = StaticFx
                hitColor = Color.white
                status = CioSEffects.static
                drawSize = 300f
                incendChance = 0.4f
                incendSpread = 5f
                incendAmount = 1
                ammoMultiplier = 1f
                ClientOnly {
                    shader = SD.TvStatic
                }
            }
        }
        DebugOnly {
            cyberionMixer = LiquidConverter("cyberion-mixer").apply {
                requirements(
                    Category.crafting, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 2),
                        ItemStack(Items.lead, 100),
                        ItemStack(Items.titanium, 100),
                    )
                )
                drawer = CyberionMixerDrawer(R.C.Holo, R.C.HoloDark)
                outputLiquid = LiquidStack(CioLiquids.cyberion, 0.3f)
                craftTime = 100f
                size = 3
                consumes.power(1.5f)
                consumes.item(Items.thorium, 1)
                consumes.liquid(Liquids.cryofluid, 0.3f)
            }

            holoProjector = HoloProjector("holo-projector").apply {
                requirements(
                    Category.units, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 14),
                        ItemStack(Items.silicon, 200),
                        ItemStack(Items.graphite, 60),
                        ItemStack(Items.thorium, 500),
                    )
                )
                plans = Seq.with(
                    HoloPlan(
                        CioUnitTypes.holoMiner,
                        Requirement(80f),
                        15f * 60f
                    ),
                    HoloPlan(
                        CioUnitTypes.holoFighter,
                        Requirement(60f),
                        11.5f * 60f
                    ),
                    HoloPlan(
                        CioUnitTypes.holoGuardian,
                        Requirement(30f),
                        8f * 60f
                    ),
                    HoloPlan(
                        CioUnitTypes.holoArchitect,
                        Requirement(100f),
                        15f * 60f
                    ),
                    HoloPlan(
                        CioUnitTypes.holoSupporter,
                        Requirement(50f),
                        15f * 60f
                    ),
                )
                size = 5
                consumes.powerCond(3f) { it: HoloProjector.HoloPBuild ->
                    it.curPlan != null && it.otherConsumersAreValid(consumes.power)
                }
            }

            aquacyberion = Floor("aqua-cyberion").apply {
                drownTime = 0f
                status = StatusEffects.freezing
                statusDuration = 240f
                speedMultiplier = 0.1f
                variants = 0
                liquidDrop = CioLiquids.cyberion
                liquidMultiplier = 0.1f
                isLiquid = true
                cacheLayer = CioCLs.cyberion
                emitLight = true
                lightRadius = 30f
                lightColor = R.C.Holo.cpy().a(0.19f)
            }

            stealth = Stealth("stealth").apply {
                requirements(
                    Category.turret, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 6),
                        ItemStack(Items.titanium, 150),
                    )
                )
                size = 3
                recoilAmount = 3f
                range = 320f
                health = 1500
                liquidCapacity = 60f
                reloadTime = 15f
                shootType = RuvikBullet(2f, 44f).apply {
                    stemVersion = STEM_VERSION.STEM2
                    width = 10f
                    height = 10f
                    hitSize = 10f
                    lifetime = 240f
                    frontColor = R.C.Holo
                    backColor = R.C.HoloDark
                }
            }

            wirelessTower = WirelessTower("wireless-tower").apply {
                requirements(
                    Category.power, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 3),
                        ItemStack(Items.copper, 200),
                        ItemStack(Items.graphite, 50),
                    )
                )
                health = 200
                distributeSpeed = 5f
                size = 2
                range = 250f
            }

            heimdall = Heimdall("heimdall").apply {
                requirements(
                    Category.turret, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 5),
                        ItemStack(Items.sporePod, 50),
                    )
                )
                size = 4
                consumes.add(
                    ConsumePower(2f, 240f, false)
                )
            }
            eye = Eye("heimdall-eye").apply {
                requirements(
                    Category.turret, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 5),
                        ItemStack(Items.sporePod, 50),
                    )
                )
                size = 2
                normalBullet = LaserBulletType(140f).apply {
                    colors = arrayOf(Pal.lancerLaser.cpy().a(0.4f), Pal.lancerLaser, Color.white)
                    hitEffect = Fx.hitLancer
                    hitSize = 4f
                    lifetime = 16f
                    drawSize = 400f
                    collidesAir = false
                    length = 173f
                    ammoMultiplier = 1f
                }
                improvedBullet = LaserBulletType(250f).apply {
                    colors = arrayOf(Pal.lancerLaser.cpy().a(0.4f), Pal.lancerLaser, Color.white)
                    hitEffect = Fx.hitLancer
                    hitSize = 10f
                    lifetime = 24f
                    drawSize = 1500f
                    collidesAir = true
                    length = 280f
                    ammoMultiplier = 1.5f
                }
            }
        }
    }

    override fun lastLoad() {
        DebugOnly {
            Blocks.powerSource.buildVisibility = BuildVisibility.shown
            Blocks.itemSource.buildVisibility = BuildVisibility.shown
            Blocks.liquidSource.buildVisibility = BuildVisibility.shown
            Blocks.payloadSource.buildVisibility = BuildVisibility.shown

            Blocks.powerVoid.buildVisibility = BuildVisibility.shown
            Blocks.itemVoid.buildVisibility = BuildVisibility.shown
            Blocks.liquidVoid.buildVisibility = BuildVisibility.shown
            Blocks.payloadVoid.buildVisibility = BuildVisibility.shown

            (Blocks.payloadConveyor as PayloadConveyor).payloadLimit = 10f
            (Blocks.payloadLoader as PayloadLoader).maxBlockSize = 10
            (Blocks.payloadRouter as PayloadRouter).payloadLimit = 10f
            (Blocks.payloadUnloader as PayloadUnloader).maxBlockSize = 10
            (Blocks.payloadPropulsionTower as PayloadMassDriver).maxPayloadSize = 10f
            /*val coreBlock = Blocks.coreShard as CoreBlock
            coreBlock.unitType = CioUnitTypes.holoFighter
            coreBlock.solid = false*/
        }
        ExperimentalOnly {
            Blocks.conveyor.sync = true
            Blocks.titaniumConveyor.sync = true
            Blocks.armoredConveyor.sync = true
            Blocks.plastaniumConveyor.sync = true
        }
    }
}