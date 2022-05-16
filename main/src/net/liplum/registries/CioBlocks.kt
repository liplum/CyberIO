package net.liplum.registries

import arc.Events
import arc.graphics.Color
import arc.graphics.Texture
import arc.struct.Seq
import arc.util.Time
import mindustry.content.*
import mindustry.entities.bullet.LaserBulletType
import mindustry.entities.bullet.LightningBulletType
import mindustry.entities.effect.MultiEffect
import mindustry.entities.pattern.ShootAlternate
import mindustry.game.EventType.Trigger
import mindustry.gen.Sounds
import mindustry.graphics.Layer
import mindustry.type.Category
import mindustry.type.ItemStack
import mindustry.type.LiquidStack
import mindustry.world.blocks.defense.OverdriveProjector
import mindustry.world.blocks.environment.Floor
import mindustry.world.blocks.payloads.*
import mindustry.world.blocks.production.GenericCrafter
import mindustry.world.blocks.sandbox.PowerSource
import mindustry.world.draw.DrawDefault
import mindustry.world.draw.DrawLiquidTile
import mindustry.world.draw.DrawMulti
import mindustry.world.meta.BuildVisibility
import net.liplum.*
import net.liplum.annotations.DependOn
import net.liplum.api.brain.UT
import net.liplum.api.brain.Upgrade
import net.liplum.api.virus.setUninfected
import net.liplum.api.virus.setUninfectedFloor
import net.liplum.blocks.cloud.Cloud
import net.liplum.blocks.data.Receiver
import net.liplum.blocks.data.Sender
import net.liplum.blocks.data.SmartDistributor
import net.liplum.blocks.data.SmartUnloader
import net.liplum.blocks.debugonly.AdjustableOverdrive
import net.liplum.blocks.deleter.Deleter
import net.liplum.blocks.icmachine.ICMachine
import net.liplum.blocks.icmachine.ICMachineS
import net.liplum.blocks.jammer.Jammer
import net.liplum.blocks.jammer.JammingLaser
import net.liplum.blocks.power.WirelessTower
import net.liplum.blocks.prism.Prism
import net.liplum.blocks.prism.PrismObelisk
import net.liplum.blocks.stream.StreamClient
import net.liplum.blocks.stream.StreamHost
import net.liplum.blocks.stream.StreamServer
import net.liplum.blocks.tmtrainer.RandomName
import net.liplum.blocks.tmtrainer.TMTRAINER
import net.liplum.blocks.underdrive.UnderdriveProjector
import net.liplum.blocks.virus.AntiVirus
import net.liplum.blocks.virus.Virus
import net.liplum.brains.*
import net.liplum.bullets.BBulletType
import net.liplum.bullets.RuvikBullet
import net.liplum.bullets.STEM_VERSION
import net.liplum.holo.*
import net.liplum.lib.animations.ganim.globalAnim
import net.liplum.seffects.StaticFx
import net.liplum.ui.DynamicContentInfoDialog.Companion.registerDynamicInfo

object CioBlocks {
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
    @JvmStatic lateinit var cyberionMixer: GenericCrafter
    @JvmStatic lateinit var holoProjector: HoloProjector
    @JvmStatic lateinit var aquacyberion: Floor
    @JvmStatic lateinit var stealth: Stealth
    @JvmStatic lateinit var wirelessTower: WirelessTower
    @JvmStatic lateinit var heimdall: Heimdall
    @JvmStatic lateinit var eye: Eye
    @JvmStatic lateinit var ear: Ear
    @JvmStatic lateinit var hand: Hand
    @JvmStatic lateinit var heart: Heart
    @DependOn("CioItems.ic")
    fun icMachine() {
        icMachine = ICMachine("ic-machine").apply {
            requirements(
                Category.crafting, arrayOf(
                    ItemStack(CioItems.ic, 2),
                    ItemStack(Items.copper, 550),
                    ItemStack(Items.lead, 280),
                    ItemStack(Items.silicon, 150),
                    ItemStack(Items.graphite, 250),
                )
            )
            health = 2000
            outputItem = ItemStack(CioItems.ic, 2)
            craftTime = 400f
            size = 3
            buildCostMultiplier = 1.5f
            craftEffect = Fx.smeltsmoke
            itemCapacity = 60
            consumeItems( //Total:100
                ItemStack(Items.copper, 50),  //50%
                ItemStack(Items.silicon, 20),  //20%
                ItemStack(Items.metaglass, 30) //30%
            )
            consumePower(10f)
        }
    }
    @DependOn("CioItems.ic")
    fun icMachineSmall() {
        icMachineSmall = ICMachineS("ic-machine-s").apply {
            requirements(
                Category.crafting, arrayOf(
                    ItemStack(Items.copper, 150),
                    ItemStack(Items.silicon, 50),
                    ItemStack(Items.graphite, 50),
                )
            )
            health = 600
            outputItem = ItemStack(CioItems.ic, 1)
            craftTime = 1150f
            size = 2
            craftEffect = Fx.formsmoke
            itemCapacity = 40
            consumeItems(
                ItemStack(Items.copper, 25),
                ItemStack(Items.sand, 40),
                ItemStack(Items.lead, 15),
            )
            consumePower(2f)

            processIcons = arrayOf(
                Items.metaglass,
                Items.silicon,
                CioItems.ic
            )
        }
    }
    @DependOn("CioItems.ic")
    fun receiver() {
        receiver = Receiver("receiver").apply {
            requirements(
                Category.distribution, arrayOf(
                    ItemStack(CioItems.ic, 1),
                    ItemStack(Items.lead, 150),
                    ItemStack(Items.graphite, 120),
                    ItemStack(Items.metaglass, 60),
                    ItemStack(Items.silicon, 180),
                )
            )
            health = 100
            consumePower(0.5f)
            replaceable = false
        }
    }
    @DependOn("CioItems.ic")
    fun sender() {
        sender = Sender("sender").apply {
            requirements(
                Category.distribution, arrayOf(
                    ItemStack(CioItems.ic, 1),
                    ItemStack(Items.lead, 150),
                    ItemStack(Items.graphite, 120),
                    ItemStack(Items.metaglass, 60),
                    ItemStack(Items.silicon, 180),
                )
            )
            health = 100
            consumePower(0.5f)
            replaceable = false
        }
    }
    @DependOn
    fun virus() {
        virus = Virus("virus").apply {
            requirements(
                Category.effect, BuildVisibility.sandboxOnly,
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
        Blocks.air.setUninfectedFloor()
        Blocks.space.setUninfectedFloor()
        Blocks.water.setUninfectedFloor()
        Blocks.deepwater.setUninfectedFloor()
        Blocks.itemSource.setUninfected()
        Blocks.liquidSource.setUninfected()
        Blocks.powerSource.setUninfected()
    }
    @DependOn("CioItems.ic")
    fun landProjector() {
        landProjector = LandProjector("land-projector").apply {
            requirements(
                Category.effect,
                if (CioMod.DebugMode)
                    BuildVisibility.sandboxOnly
                else
                    BuildVisibility.hidden, arrayOf(
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
            requirements(
                Category.effect, BuildVisibility.shown, arrayOf(
                    ItemStack(CioItems.ic, 4),
                    ItemStack(Items.copper, 300),
                    ItemStack(Items.lead, 20),
                    ItemStack(Items.silicon, 240),
                    ItemStack(Items.plastanium, 10),
                    ItemStack(Items.phaseFabric, 5),
                )
            )
            health = 300
            color = R.C.LightBlue
            maxSlowDownRate = 0.9f
            powerProduction = 4.5f
            maxPowerEFFBlocksReq = 22
            maxGear = 5
            size = 1
        }
    }
    @DependOn("CioItems.ic")
    fun antiVirus() {
        antiVirus = AntiVirus("anti-virus").apply {
            requirements(
                Category.effect, BuildVisibility.shown, arrayOf(
                    ItemStack(CioItems.ic, 1),
                    ItemStack(Items.copper, 100),
                    ItemStack(Items.graphite, 40),
                    ItemStack(Items.silicon, 25)
                )
            )
            health = 500
            consumePower(0.5f)
            size = 1
        }.setUninfected()
    }
    @DependOn("CioItems.ic")
    fun cloud() {
        cloud = Cloud("cloud").apply {
            requirements(
                Category.effect,
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
            consumePower(1f)
        }
    }
    @DependOn
    fun hyperOverdriveSphere() {
        hyperOverdriveSphere = AdjustableOverdrive("hyper-overdrive-sphere").apply {
            requirements(
                Category.effect,
                if (CioMod.DebugMode)
                    BuildVisibility.shown
                else
                    BuildVisibility.sandboxOnly, arrayOf()
            )
            DebugOnly {
                buildVisibility = BuildVisibility.shown
            }
            size = 3
            maxBoost = 50f
            minBoost = 0.5f
            consumePower(50f)
            speedBoost = 50f
            range = 1000f
        }
    }
    @DependOn("CioItems.ic")
    fun prism() {
        prism = Prism("prism").apply {
            requirements(
                Category.turret, BuildVisibility.shown, arrayOf(
                    ItemStack(CioItems.ic, 10),
                    ItemStack(Items.copper, 250),
                    ItemStack(Items.metaglass, 350),
                    ItemStack(Items.titanium, 50),
                )
            )
            buildCostMultiplier = 2f
            size = 4
            health = 2500
        }
    }
    @DependOn(
        "CioItems.ic",
        "CioBlocks.prism"
    )
    fun prismObelisk() {
        prismObelisk = PrismObelisk("prism-obelisk").apply {
            requirements(
                Category.turret, BuildVisibility.shown, arrayOf(
                    ItemStack(CioItems.ic, 3),
                    ItemStack(Items.copper, 60),
                    ItemStack(Items.plastanium, 120),
                    ItemStack(Items.metaglass, 240),
                    ItemStack(Items.titanium, 10),
                )
            )
            size = 2
            health = 1000
            prismType = prism
        }
    }
    @DependOn("CioItems.ic")
    fun deleter() {
        deleter = Deleter("deleter").apply {
            requirements(
                Category.turret, BuildVisibility.shown, arrayOf(
                    ItemStack(CioItems.ic, 9),
                    ItemStack(Items.graphite, 100),
                    ItemStack(Items.silicon, 60),
                    ItemStack(Items.thorium, 250),
                    ItemStack(Items.surgeAlloy, 50),
                )
            )
            range = 180f
            cooldownTime
            //cooldown = 0.1f
            recoil = 5f
            reload = 15f
            consumePower(3f)
            size = 3
            buildCostMultiplier = 1.5f
            extraLostHpBounce = 0.005f
            health = 300 * size * size
            shootSound = Sounds.lasershoot
            configBullet {
                damage = 0.5f
                pierceCap = 3
            }
        }
    }
    @DependOn("CioItems.ic")
    fun holoWall() {
        holoWall = HoloWall("holo-wall").apply {
            requirements(
                Category.defense, BuildVisibility.shown, arrayOf(
                    ItemStack(CioItems.ic, 1),
                    ItemStack(Items.silicon, 6),
                    ItemStack(Items.titanium, 12),
                    ItemStack(Items.plastanium, 10),
                )
            )
            size = 1
            restoreReload = 10 * 60f
            health = 600
            buildCostMultiplier = 3.5f
        }
    }
    @DependOn(
        "CioItems.ic",
        "CioBlocks.holoWall"
    )
    fun holoWallLarge() {
        holoWallLarge = HoloWall("holo-wall-large").apply {
            requirements(
                Category.defense, BuildVisibility.shown, arrayOf(
                    ItemStack(CioItems.ic, 2),
                    ItemStack(Items.silicon, 24),
                    ItemStack(Items.titanium, 48),
                    ItemStack(Items.plastanium, 40),
                )
            )
            size = 2
            restoreReload = 15 * 60f
            health = 550 * 5
            buildCostMultiplier = 4.5f
        }
    }
    @DependOn(
        "CioItems.ic",
        "CioBulletTypes.radiationInterference",
        "CioBulletTypes.virus",
    )
    fun TMTRAINER() {
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
                Items.sporePod,
                CioBulletTypes.virus,
                Items.thorium, CioBulletTypes.radiationInterference,
            )
            inaccuracy = 1f
            rotateSpeed = 10f
            maxAmmo = 80
            shoot = ShootAlternate().apply {
                spread = 4f
            }
            reload = 5f
            recoilPow
            range = 260f
            shootCone = 15f
            size = 4
            health = 250 * size * size
            //limitRange(20f)
            ClientOnly {
                Events.run(Trigger.update) {
                    if (Time.globalTime % CioMod.UpdateFrequency < 1f) {
                        TMTRAINER.localizedName = RandomName.one(8)
                        TMTRAINER.description = RandomName.one(25)
                    }
                }
            }
        }.registerDynamicInfo()
    }
    @DependOn("CioItems.ic")
    fun smartDistributor() {
        smartDistributor = SmartDistributor("smart-distributor").apply {
            requirements(
                Category.distribution, BuildVisibility.shown, arrayOf(
                    ItemStack(CioItems.ic, 15),
                    ItemStack(Items.copper, 550),
                    ItemStack(Items.silicon, 210),
                    ItemStack(Items.plastanium, 80),
                    ItemStack(Items.thorium, 140),
                    ItemStack(Items.surgeAlloy, 50),
                )
            )
            health = 4500
            size = 2
        }
    }
    @DependOn("CioItems.ic")
    fun smartUnloader() {
        smartUnloader = SmartUnloader("smart-unloader").apply {
            requirements(
                Category.distribution, BuildVisibility.shown, arrayOf(
                    ItemStack(CioItems.ic, 5),
                    ItemStack(Items.lead, 350),
                    ItemStack(Items.silicon, 210),
                    ItemStack(Items.graphite, 150),
                    ItemStack(Items.titanium, 50),
                )
            )
            unloadSpeed = 5f
            health = 1500
            size = 2
        }
    }
    @DependOn("CioItems.ic")
    fun streamClient() {
        streamClient = StreamClient("stream-client").apply {
            requirements(
                Category.liquid, BuildVisibility.shown, arrayOf(
                    ItemStack(CioItems.ic, 2),
                    ItemStack(Items.metaglass, 40),
                    ItemStack(Items.silicon, 20),
                    ItemStack(Items.graphite, 40),
                    ItemStack(Items.titanium, 10),
                )
            )
            health = 300
            consumePower(0.7f)
            liquidCapacity = 300f
            replaceable = false
        }
    }
    @DependOn("CioItems.ic")
    fun streamHost() {
        streamHost = StreamHost("stream-host").apply {
            requirements(
                Category.liquid, BuildVisibility.shown, arrayOf(
                    ItemStack(CioItems.ic, 5),
                    ItemStack(Items.lead, 100),
                    ItemStack(Items.metaglass, 600),
                    ItemStack(Items.silicon, 80),
                    ItemStack(Items.graphite, 60),
                    ItemStack(Items.titanium, 40),
                    ItemStack(Items.plastanium, 20),
                )
            )
            health = 2000
            size = 2
            powerUseBase = 1f
            networkSpeed = 3f
            liquidCapacity = 800f
            replaceable = false
            maxConnection = 3
        }
    }
    @DependOn("CioItems.ic")
    fun streamServer() {
        streamServer = StreamServer("stream-server").apply {
            requirements(
                Category.liquid, BuildVisibility.shown, arrayOf(
                    ItemStack(CioItems.ic, 16),
                    ItemStack(Items.copper, 1200),
                    ItemStack(Items.lead, 400),
                    ItemStack(Items.metaglass, 1200),
                    ItemStack(Items.silicon, 320),
                    ItemStack(Items.thorium, 40),
                    ItemStack(Items.phaseFabric, 120),
                )
            )
            health = 5000
            size = 3
            researchCostMultiplier = 0.7f
            maxConnection = 5
            powerUseBase = 2f
            powerUsePerConnection = 2f
            networkSpeed = 15f
            liquidCapacity = 2000f
            replaceable = false
        }
    }
    @DependOn("CioItems.ic")
    fun jammer() {
        jammer = Jammer("jammer").apply {
            requirements(
                Category.turret, BuildVisibility.shown, arrayOf(
                    ItemStack(CioItems.ic, 8),
                    ItemStack(Items.lead, 350),
                    ItemStack(Items.thorium, 200),
                    ItemStack(Items.surgeAlloy, 150),
                )
            )
            size = 3
            health = 250 * size * size
            shootEffect = StaticFx
            shootCone = 40f
            recoil = 4f
            shake = 2f
            shootDuration = 150f
            range = 195f
            coolant = consumeCoolant(0.3f)
            reload = 40f
            firingMoveFract = 1f
            shootDuration = 180f
            shootSound = CioSounds.jammerPreShoot
            loopSound = CioSounds.tvStatic
            loopSoundVolume = 0.5f
            rotateSpeed = 2f
            consumePower(15f)

            shootType = JammingLaser().apply {
                length = 220f
                divisions = 5
                hitEffect = StaticFx
                hitColor = Color.white
                status = CioSEffects.static
                drawSize = 300f
                incendChance = 0.4f
                incendSpread = 5f
                incendAmount = 1
                ammoMultiplier = 1f
                damage = 120f
            }
        }
    }
    @DependOn(
        "CioItems.ic",
        "CioLiquids.cyberion"
    )
    fun cyberionMixer() {
        cyberionMixer = GenericCrafter("cyberion-mixer").apply {
            requirements(
                Category.crafting, BuildVisibility.shown, arrayOf(
                    ItemStack(CioItems.ic, 4),
                    ItemStack(Items.lead, 100),
                    ItemStack(Items.titanium, 100),
                    ItemStack(Items.metaglass, 50),
                )
            )
            health = 100 * size * size
            liquidCapacity = 20f
            // TODO: use default drawer temporarily
            //drawer = CyberionMixerDrawer(R.C.Holo, R.C.HoloDark)
            drawer = DrawMulti(DrawLiquidTile(CioLiquids.cyberion), DrawDefault())
            outputLiquid = LiquidStack(CioLiquids.cyberion, 0.3f)
            craftTime = 100f
            size = 3
            consumePower(1.5f)
            consumeItem(Items.thorium, 1)
            consumeLiquid(Liquids.cryofluid, 0.3f)
        }
    }
    @DependOn(
        "CioItems.ic",
        "CioLiquids.cyberion",
        "CioUnitTypes.holoMiner",
        "CioUnitTypes.holoFighter",
        "CioUnitTypes.holoGuardian",
        "CioUnitTypes.holoArchitect",
        "CioUnitTypes.holoSupporter",
    )
    fun holoProjector() {
        holoProjector = HoloProjector("holo-projector").apply {
            requirements(
                Category.units, BuildVisibility.shown, arrayOf(
                    ItemStack(CioItems.ic, 16),
                    ItemStack(Items.silicon, 220),
                    ItemStack(Items.graphite, 300),
                    ItemStack(Items.metaglass, 500),
                    ItemStack(Items.thorium, 1200),
                )
            )
            researchCostMultiplier = 0.8f
            health = 3500
            plans = Seq.with(
                HoloPlan(
                    CioUnitTypes.holoMiner,
                    Requirement(300f),
                    15f * 60f
                ),
                HoloPlan(
                    CioUnitTypes.holoFighter,
                    Requirement(300f),
                    15f * 60f
                ),
                HoloPlan(
                    CioUnitTypes.holoGuardian,
                    Requirement(155f),
                    7.5f * 60f
                ),
                HoloPlan(
                    CioUnitTypes.holoArchitect,
                    Requirement(600f),
                    25f * 60f
                ),
                HoloPlan(
                    CioUnitTypes.holoSupporter,
                    Requirement(250f),
                    12f * 60f
                ),
            )
            size = 5
            buildCostMultiplier = 2f
        }
    }
    @DependOn("CioLiquids.cyberion")
    fun aquacyberion() {
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
    }
    @DependOn(
        "CioItems.ic",
        "CioBulletTypes.ruvik2"
    )
    fun stealth() {
        stealth = Stealth("stealth").apply {
            requirements(
                Category.turret, BuildVisibility.shown, arrayOf(
                    ItemStack(CioItems.ic, 5),
                    ItemStack(Items.titanium, 150),
                    ItemStack(Items.plastanium, 30),
                )
            )
            size = 3
            recoil = 3f
            range = 260f
            health = 1500
            liquidCapacity = 60f
            reload = 15f
            shootType = RuvikBullet(2f, 100f).apply {
                stemVersion = STEM_VERSION.STEM2
                width = 10f
                height = 10f
                hitSize = 10f
                lifetime = 240f
                maxRange = range
                frontColor = R.C.Holo
                backColor = R.C.HoloDark
            }
        }
    }
    @DependOn("CioItems.ic")
    fun wirelessTower() {
        wirelessTower = WirelessTower("wireless-tower").apply {
            requirements(
                Category.power, BuildVisibility.shown, arrayOf(
                    ItemStack(CioItems.ic, 2),
                    ItemStack(Items.copper, 310),
                    ItemStack(Items.lead, 20),
                    ItemStack(Items.silicon, 20),
                    ItemStack(Items.graphite, 30),
                )
            )
            health = 600
            distributeSpeed = 10f
            size = 2
            range = 300f
        }
    }
    @DependOn("CioItems.ic")
    fun heimdall() {
        heimdall = Heimdall("heimdall").apply {
            requirements(
                Category.turret, BuildVisibility.shown, arrayOf(
                    ItemStack(CioItems.ic, 10),
                    ItemStack(Items.sporePod, 300),
                    ItemStack(Items.thorium, 150),
                    ItemStack(Items.metaglass, 50),
                    ItemStack(Items.copper, 120),
                    ItemStack(Items.silicon, 180),
                    ItemStack(Items.plastanium, 50),
                )
            )
            size = 4
            range = 175f
            health = 400 * size * size
            powerUse = 2.5f
            connectedSound = CioSounds.connected
            addFormationPatterns(
                FaceFE, FunnyFaceFE, ForceFieldFE
            )
        }
    }
    @DependOn("CioItems.ic")
    fun eye() {
        eye = Eye("heimdall-eye").apply {
            requirements(
                Category.turret, BuildVisibility.shown, arrayOf(
                    ItemStack(CioItems.ic, 2),
                    ItemStack(Items.pyratite, 15),
                    ItemStack(Items.plastanium, 10),
                    ItemStack(Items.metaglass, 40),
                    ItemStack(Items.copper, 60),
                    ItemStack(Items.silicon, 30),
                )
            )
            range = 165f
            size = 2
            health = 300 * size * size
            consumePower(3f)
            moveWhileCharging = false
            shoot.firstShotDelay = 60f
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
            normalBullet = LightningBulletType().apply {
                damage = 90f
                lightningLength = 25
                collidesAir = false
                ammoMultiplier = 1f
                recoil = 3f
                shootCone = 3f
                accurateDelay = true
                lightningColor = R.C.RedAlert
            }
            improvedSounds = CioSounds.laser
            improvedBullet = LaserBulletType(250f).apply {
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
            requirements(
                Category.turret, BuildVisibility.shown, arrayOf(
                    ItemStack(CioItems.ic, 2),
                    ItemStack(Items.sporePod, 20),
                    ItemStack(Items.graphite, 5),
                    ItemStack(Items.copper, 60),
                    ItemStack(Items.silicon, 50),
                    ItemStack(Items.plastanium, 10),
                )
            )
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
            range = 145f
            size = 2
            damage = 8f
            health = 300 * size * size
        }
    }
    @DependOn("CioItems.ic")
    fun hand() {
        DebugOnly {
            hand = Hand("heimdall-hand").apply {
                requirements(
                    Category.turret, BuildVisibility.shown, arrayOf(
                        ItemStack(CioItems.ic, 5),
                        ItemStack(Items.copper, 80),
                        ItemStack(Items.silicon, 40),
                        ItemStack(Items.phaseFabric, 40),
                    )
                )
            }
        }
    }
    @DependOn("CioItems.ic")
    fun heart() {
        DebugOnly {
            heart = Heart("heimdall-heart").apply {
                requirements(
                    Category.turret, BuildVisibility.shown, arrayOf(
                    )
                )
                size = 4
                convertSpeed = 8f
                blood = Blood()
                heartbeat.apply {
                    shake.config {
                        base = 1.5f
                        upRange = 4.8f - base
                        downRange = 1.0f
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
                    sounds = arrayOf(
                        CioSounds.heartbeat,
                        CioSounds.heartbeatFaster,
                    )
                    soundIndexer = {
                        when (it) {
                            in Float.MIN_VALUE..0.1f -> 0
                            in 0.1f..Float.MAX_VALUE -> 1
                            else -> 0
                        }
                    }
                    offset = 20f // +5f when improved
                }
                bulletType = BBulletType("blood-bullet".Cio).apply {
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
    @DependOn
    fun _overwriteVanilla() {
        DebugOnly {
            (Blocks.powerSource as PowerSource).apply {
                buildVisibility = BuildVisibility.shown
                health = Int.MAX_VALUE
            }
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