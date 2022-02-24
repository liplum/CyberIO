package net.liplum.registries

import mindustry.content.Blocks
import mindustry.content.Fx
import mindustry.content.Items
import mindustry.content.Liquids
import mindustry.ctype.ContentList
import mindustry.entities.bullet.LaserBulletType
import mindustry.type.Category
import mindustry.type.ItemStack
import mindustry.world.blocks.defense.OverdriveProjector
import mindustry.world.blocks.production.GenericCrafter
import mindustry.world.meta.BuildVisibility
import net.liplum.DebugOnly
import net.liplum.R
import net.liplum.animations.ganim.animation
import net.liplum.api.virus.setUninfected
import net.liplum.api.virus.setUninfectedFloor
import net.liplum.blocks.cloud.Cloud
import net.liplum.blocks.holo.HoloFloor
import net.liplum.blocks.holo.HoloWall
import net.liplum.blocks.holo.LandProjector
import net.liplum.blocks.icmachine.ICMachine
import net.liplum.blocks.prism.Prism
import net.liplum.blocks.rs.Receiver
import net.liplum.blocks.rs.Sender
import net.liplum.blocks.underdrive.UnderdriveProjector
import net.liplum.blocks.virus.AntiVirus
import net.liplum.blocks.virus.Virus
import net.liplum.utils.CioDebugOnly

class CioBlocks : ContentList {
    companion object {
        @JvmStatic lateinit var icMachine: GenericCrafter
        @JvmStatic lateinit var receiver: Receiver
        @JvmStatic lateinit var sender: Sender
        @JvmStatic lateinit var virus: Virus
        @JvmStatic lateinit var landProjector: LandProjector
        @JvmStatic lateinit var holoFloor: HoloFloor
        @JvmStatic lateinit var underdriveProjector: UnderdriveProjector
        @JvmStatic lateinit var antiVirus: AntiVirus
        @JvmStatic lateinit var cloud: Cloud
        @JvmStatic lateinit var prism: Prism
        @CioDebugOnly
        @JvmStatic var hyperOverdriveSphere: OverdriveProjector? = null
        @JvmStatic lateinit var holoWall: HoloWall
        @JvmStatic lateinit var holoWallLarge: HoloWall
    }

    override fun load() {
        icMachine = ICMachine("ic-machine").apply {
            requirements(
                Category.crafting, arrayOf(
                    ItemStack(Items.copper, 1000),
                    ItemStack(Items.silicon, 200),
                    ItemStack(Items.graphite, 150),
                    ItemStack(Items.titanium, 250)
                )
            )
            health = 2000
            outputItem = ItemStack(CioItems.ic, 1)
            craftTime = 1200f
            size = 3
            buildCostMultiplier = 10f
            craftEffect = Fx.smelt
            itemCapacity = 200
            consumes.items( //Total:200
                ItemStack(Items.silicon, 40),  //20%
                ItemStack(Items.copper, 100),  //50%
                ItemStack(Items.metaglass, 60) //30%
            )
            consumes.power(10f)
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
                    ItemStack(Items.silicon, 10)
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
        }.animation(60f, 3)

        Blocks.air.setUninfectedFloor()
        Blocks.space.setUninfectedFloor()
        Blocks.water.setUninfectedFloor()
        Blocks.deepwater.setUninfectedFloor()
        Blocks.itemSource.setUninfected()
        Blocks.liquidSource.setUninfected()
        Blocks.powerSource.setUninfected()

        landProjector = LandProjector("land-projector").apply {
            requirements(
                Category.logic, BuildVisibility.sandboxOnly, arrayOf(
                    ItemStack(CioItems.ic, 3),
                    ItemStack(Items.graphite, 80),
                    ItemStack(Items.thorium, 100),
                    ItemStack(Items.silicon, 50)
                )
            )
            health = 100
            size = 2
        }

        holoFloor = HoloFloor("holo-floor").apply {
            variants = 3
        }.setUninfectedFloor()

        underdriveProjector = UnderdriveProjector("underdrive-projector").apply {
            requirements(
                Category.effect, arrayOf(
                    ItemStack(CioItems.ic, 1),
                    ItemStack(Items.copper, 200),
                    ItemStack(Items.coal, 100),
                    ItemStack(Items.metaglass, 75),
                )
            )
            health = 100
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
            consumes.power(0.5f)
            size = 1
        }.setUninfected()

        cloud = Cloud("cloud").apply {
            requirements(
                Category.logic, BuildVisibility.sandboxOnly, arrayOf(
                )
            )
            size = 3
            consumes.power(1f)
        }

        DebugOnly {
            hyperOverdriveSphere = OverdriveProjector("hyper-overdrive-sphere").apply {
                requirements(
                    Category.effect, BuildVisibility.sandboxOnly, arrayOf(
                    )
                )
                size = 3
                consumes.power(50f)
                speedBoost = 50f
                range = 1000f
                hasBoost = false
            }
        }
        DebugOnly {
            prism = object : Prism("prism") {
                init {
                    requirements(
                        Category.turret, BuildVisibility.sandboxOnly, arrayOf()
                    )
                    size = 3
                    range = 330f
                    health = 1500
                    consumes.liquid(Liquids.water, 1f)
                    shootType = object : LaserBulletType(100f) {
                    }
                }
            }
        }
        holoWall = HoloWall("holo-wall").apply {
            requirements(
                Category.defense, BuildVisibility.sandboxOnly, arrayOf(
                    ItemStack(CioItems.ic, 1),
                    ItemStack(Items.titanium, 20),
                )
            )
            size = 1
            restoreReload = 10 * 60f
            health = 300
        }

        holoWallLarge = HoloWall("holo-wall-large").apply {
            requirements(
                Category.defense, BuildVisibility.sandboxOnly, arrayOf(
                    ItemStack(CioItems.ic, 3),
                    ItemStack(Items.titanium, 30 * 4),
                    ItemStack(Items.silicon, 10),
                )
            )
            size = 2
            restoreReload = 15 * 60f
            health = 300 * 5
        }
    }
}