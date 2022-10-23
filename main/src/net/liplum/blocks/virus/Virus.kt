package net.liplum.blocks.virus

import arc.func.Prov
import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.math.Mathf
import arc.struct.Seq
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.Vars
import mindustry.content.Blocks
import mindustry.entities.units.BuildPlan
import mindustry.game.Team
import mindustry.gen.Building
import mindustry.gen.Call
import mindustry.graphics.Layer
import mindustry.graphics.Pal
import mindustry.world.Block
import mindustry.world.Tile
import net.liplum.DebugOnly
import net.liplum.R
import net.liplum.api.virus.UninfectedBlocksRegistry
import net.liplum.common.UseRandom
import net.liplum.common.shader.use
import plumy.dsl.bundle
import net.liplum.common.util.off
import net.liplum.common.util.on
import net.liplum.utils.ServerOnly
import plumy.core.Else
import plumy.dsl.AddBar
import net.liplum.utils.sub
import net.liplum.registry.SD
import net.liplum.utils.yesNo
import plumy.core.ClientOnly
import plumy.core.Serialized
import plumy.core.assets.EmptyTR

typealias UBR = UninfectedBlocksRegistry

private val Number2X = arrayOf(
    -1, -1, -1,
    0, 0,
    1, 1, 1
)
private val Number2Y = arrayOf(
    -1, 0, 1,
    -1, 1,
    -1, 0, 1
)
private const val OmniInfected = 0b1111_1111

open class Virus(name: String) : Block(name) {
    /**
     * The lager the number the slower the spreading speed. Belongs to [0,+inf)
     */
    @JvmField var spreadingSpeed: Int = 1000
    /**
     * The maximum number of a virus can produce. -1 means unlimited.
     */
    @JvmField var maxReproductionScale: Int = -1
    @JvmField var inheritChildrenNumber: Boolean = false
    /**
     * The maximum number of the generation of the ZERO virus can produce. -1 means unlimited.
     */
    @JvmField var maxGeneration: Int = -1
    @JvmField var mutationRate: Int = 1
    /**
     * The maximum number of the mutation of the ZERO virus can produce. -1 means unlimited.
     */
    @JvmField var maxMutationNumber: Int = -1
    @JvmField var canMutate: Boolean = false
    @JvmField var startMutationPercent: Float = 0.8f
    @ClientOnly @JvmField var raceMaskTR = EmptyTR

    init {
        buildType = Prov { VirusBuild() }
        solid = true
        update = true
        canOverdrive = true
        size = 1
        sync = true
    }

    override fun load() {
        super.load()
        raceMaskTR = this.sub("race-mask")
    }

    open val maxGenerationOrDefault: Int
        get() = if (maxGeneration == -1) 100 else maxGeneration
    /**
     * This can be only placed individually.
     */
    override fun handlePlacementLine(plans: Seq<BuildPlan>) {
        val first = plans.first()
        plans.clear()
        plans.add(first)
    }

    override fun setBars() {
        super.setBars()
        AddBar<VirusBuild>(R.Bar.GenerationN,
            { R.Bar.Generation.bundle(curGeneration) },
            { R.C.VirusBK },
            { curGeneration / maxGenerationOrDefault.toFloat() }
        )
        DebugOnly {
            AddBar<VirusBuild>(R.Bar.IsAliveN,
                { R.Bar.IsAlive.bundle(isAlive.yesNo()) },
                { R.C.IsAive },
                { if (isAlive) 1f else 0f }
            )
            AddBar<VirusBuild>(R.Bar.NeighborStateN,
                { "${neighborState.countOneBits()}" },
                { Pal.bar },
                { neighborState.countOneBits() / 8f }
            )
        }
    }

    override fun minimapColor(tile: Tile) = R.C.VirusBK.rgba()
    open inner class VirusBuild : Building(), IVirusBuilding {
        @Serialized
        var neighborState: Int = 0
        @Serialized
        var curGeneration: Int = 0
        @Serialized
        var curChildrenNumber: Int = 0
        @Serialized
        var curVarianceNumber: Int = 0
        @Serialized
        var isAlive: Boolean = true
        @Serialized
        var raceColor: Color? = null
        override fun onProximityUpdate() {
            refreshNeighborState()
            if (neighborState != OmniInfected) {
                noSleep()
            }
        }

        open fun refreshNeighborState() {
            val selfX = tile.x.toInt()
            val selfY = tile.y.toInt()
            for (r in 0..7) {
                val tested = Vars.world.tile(
                    selfX + Number2X[r],
                    selfY + Number2Y[r]
                )
                if (VirusU.canInfect(tested)) {
                    neighborState = neighborState off r
                }
            }
        }
        @ServerOnly
        @UseRandom
        override fun updateTile() {
            ServerOnly {
                if (isDead) {
                    return
                }
                if (selfOnUninfectedFloorOrOverLay) {
                    setDead()
                }
                if (isDead) {
                    return
                }
                if (neighborState == OmniInfected) {
                    sleep()
                    return
                }
                if (canReproduce) {
                    var speed = spreadingSpeed
                    if (canOverdrive) {
                        speed = (speed / timeScale).toInt()
                    }
                    val luckyNumber = Mathf.random(speed)
                    if (luckyNumber == 0) {
                        /*
                        5   6   7   ( 1,-1)  ( 1, 0)  ( 1, 1)
                        3   *   4   ( 0,-1)  ( 0, 0)  ( 0, 1)
                        0   1   2   (-1,-1)  (-1, 0)  (-1, 1)
                        */
                        val r = Mathf.random(0, 7)
                        val selfX = tile.x.toInt()
                        val selfY = tile.y.toInt()
                        val infected = Vars.world.tile(
                            selfX + Number2X[r],
                            selfY + Number2Y[r]
                        )
                        if (VirusU.canInfect(infected)) {
                            this.reproduce(infected)
                        }
                        neighborState = neighborState on r
                    }
                } else {
                    sleep()
                }
            }
        }

        override fun draw() {
            DebugOnly {
                SD.DynamicColor.use(Layer.block) {
                    Draw.rect(block.region, x, y)
                    if (raceColor != null) {
                        Draw.color(raceColor)
                        Draw.rect(raceMaskTR, x, y)
                        Draw.color()
                    }
                }
            } Else {
                Draw.rect(block.region, x, y)
                if (raceColor != null) {
                    Draw.color(raceColor)
                    Draw.rect(raceMaskTR, x, y)
                    Draw.color()
                }
                Draw.reset()
            }
        }

        open val canReproduce: Boolean
            get() {
                val RS = if (maxReproductionScale == -1) true
                else curChildrenNumber < maxReproductionScale
                val G = if (maxGeneration == -1) true
                else curGeneration < maxGeneration
                return RS && G
            }

        open fun reproduce(infected: Tile) {
            if (newChildNeedMutate) {
                reproduceVariant(infected)
            } else {
                reproduceNormal(infected)
            }
        }

        open fun reproduceNormal(infected: Tile) {
            Call.setTile(infected, this@Virus, team, 0)
            val newGen = infected.build as? VirusBuild
            newGen?.curGeneration = this.curGeneration + 1
            curChildrenNumber++
            if (inheritChildrenNumber) {
                newGen?.curChildrenNumber = this.curChildrenNumber
            }
            newGen?.raceColor = this.raceColor
        }

        open fun infectThat() {
        }

        open fun reproduceVariant(infected: Tile) {
            Call.setTile(infected, this@Virus, team, 0)
            val newGen = infected.build as? VirusBuild
            curChildrenNumber++
            newGen?.raceColor = VirusColors.randomColor(this.raceColor)
        }

        open val curMutateRate: Int
            get() {
                val smg = maxGenerationOrDefault * startMutationPercent//Start mutation generation
                val factor = (1f / (smg * smg)) * curGeneration * curGeneration
                return (mutationRate * factor).toInt()
            }
        open val newChildNeedMutate: Boolean
            get() {
                if (!canMutate) {
                    return false
                }
                if (maxMutationNumber != -1 && curVarianceNumber >= maxMutationNumber) {
                    return false
                }
                return Mathf.random(100) < curMutateRate
            }
        open val selfOnUninfectedFloorOrOverLay: Boolean
            get() = UBR.hasFloor(tile.floor()) ||
                    UBR.hasOverlay(tile.overlay())

        open fun setDead() {
            if (tile.block() == this@Virus) {
                isAlive = false
                Call.setTile(tile, Blocks.air, Team.derelict, 0)
            }
        }

        override fun write(write: Writes) {
            super.write(write)
            write.s(curGeneration)
            write.s(curChildrenNumber)
            write.s(curVarianceNumber)
            write.i(raceColor?.rgba() ?: -1)
            write.bool(isAlive)
            write.s(neighborState)
        }

        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            curGeneration = read.s().toInt()
            curChildrenNumber = read.s().toInt()
            curVarianceNumber = read.s().toInt()
            val raceColorNumber = read.i()
            raceColor = if (raceColorNumber == -1) null else Color(raceColorNumber)
            isAlive = read.bool()
            neighborState = read.s().toInt()
        }

        override fun killVirus() {
            setDead()
        }

        override val isDead: Boolean
            get() = !isAlive
    }
}