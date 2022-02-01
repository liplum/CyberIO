package net.liplum.registries

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.TextureRegion
import arc.math.Mathf
import mindustry.content.Fx
import mindustry.content.Items
import mindustry.ctype.ContentList
import mindustry.graphics.Drawf
import mindustry.type.Category
import mindustry.type.ItemStack
import mindustry.world.blocks.production.GenericCrafter
import mindustry.world.meta.BuildVisibility
import net.liplum.animations.animation
import net.liplum.animations.anims.IAnimated
import net.liplum.animations.anis.AniConfig
import net.liplum.animations.anis.AniState
import net.liplum.blocks.*
import net.liplum.blocks.floors.HoloFloor
import net.liplum.utils.AnimUtil
import net.liplum.utils.AtlasUtil

class CioBlocks : ContentList {
    companion object {
        @JvmStatic lateinit var icMachine: GenericCrafter
        @JvmStatic lateinit var receiver: Receiver
        @JvmStatic lateinit var sender: Sender
        @JvmStatic lateinit var virus: Virus
        @JvmStatic lateinit var landProjector: LandProjector
        @JvmStatic lateinit var holoFloor: HoloFloor
    }

    override fun load() {
        icMachine = object : AniedCrafter("ic-machine") {
            lateinit var idleState: AniState<AniedCrafter, AniedCrafterBuild>
            lateinit var workingState: AniState<AniedCrafter, AniedCrafterBuild>
            lateinit var workingAnimation: IAnimated
            lateinit var idleTR: TextureRegion

            init {
                requirements(
                    Category.crafting, arrayOf(
                        ItemStack(Items.copper, 1000),
                        ItemStack(Items.silicon, 200),
                        ItemStack(Items.graphite, 150),
                        ItemStack(Items.titanium, 250)
                    )
                )
                outputItem = ItemStack(CioItems.ic, 1)
                craftTime = 1200f
                size = 3
                buildCostMultiplier = 10f
                craftEffect = Fx.smelt
                hasPower = true
                hasItems = true
                itemCapacity = 200
                consumes.items( //Total:200
                    ItemStack(Items.silicon, 40),  //20%
                    ItemStack(Items.copper, 100),  //50%
                    ItemStack(Items.metaglass, 60) //30%
                )
                consumes.power(10f)
            }

            override fun genAnimState() {
                idleState = addAniState("Idle") { _, build ->
                    Draw.rect(idleTR, build.x, build.y)
                }
                workingState = addAniState("Working") { _, build ->
                    workingAnimation.draw(build.x, build.y, build)
                    Drawf.light(
                        build.team, build.x, build.y,
                        5f,
                        Color.white,
                        1f
                    )
                }
            }

            override fun genAniConfig() {
                aniConfig = AniConfig()
                aniConfig.defaultState(idleState)
                aniConfig.enter(idleState, workingState) { _, build ->
                    !Mathf.zero(build.progress)
                }
                aniConfig.enter(workingState, idleState) { _, build ->
                    Mathf.zero(build.progress) && Mathf.zero(build.power.status)
                }
                aniConfig.build()
            }

            override fun load() {
                super.load()
                workingAnimation = AnimUtil.auto(this, "indicator-light", 7, 60f)
                idleTR = AtlasUtil.sub(this, "light-off")
            }
        }

        receiver = object : Receiver("receiver") {
            init {
                requirements(
                    Category.distribution, arrayOf(
                        ItemStack(CioItems.ic, 1),
                        ItemStack(Items.copper, 50),
                        ItemStack(Items.graphite, 20),
                        ItemStack(Items.metaglass, 20),
                        ItemStack(Items.silicon, 10)
                    )
                )
                consumes.power(0.5f)
                replaceable = false
            }
        }

        sender = object : Sender("sender") {
            init {
                requirements(
                    Category.distribution, arrayOf(
                        ItemStack(CioItems.ic, 1),
                        ItemStack(Items.copper, 50),
                        ItemStack(Items.graphite, 20),
                        ItemStack(Items.metaglass, 20),
                        ItemStack(Items.silicon, 10)
                    )
                )
                consumes.power(0.5f)
                replaceable = false
            }
        }

        virus = object : Virus("virus") {
            init {
                requirements(Category.logic, BuildVisibility.sandboxOnly, ItemStack.with())
                spreadingSpeed = 200
            }
        }.animation(60f, 3)

        landProjector = object : LandProjector("land-projector") {
            init {
                requirements(
                    Category.logic, BuildVisibility.sandboxOnly, arrayOf(
                        ItemStack(CioItems.ic, 2),
                        ItemStack(Items.graphite, 80),
                        ItemStack(Items.titanium, 40),
                        ItemStack(Items.silicon, 20)
                    )
                )
            }
        }

        holoFloor = object : HoloFloor("holo-floor") {}
    }
}