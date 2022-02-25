package net.liplum.blocks.virus

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.TextureRegion
import arc.math.Mathf
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.Vars
import mindustry.content.Blocks
import mindustry.game.Team
import mindustry.gen.Building
import mindustry.gen.Call
import mindustry.graphics.Layer
import mindustry.graphics.Pal
import mindustry.ui.Bar
import mindustry.world.Tile
import net.liplum.DebugOnly
import net.liplum.R
import net.liplum.ServerOnly
import net.liplum.UseRandom
import net.liplum.api.virus.UninfectedBlocksRegistry
import net.liplum.blocks.AnimedBlock
import net.liplum.registries.ShaderRegistry
import net.liplum.utils.*

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

open class Virus(name: String) : AnimedBlock(name) {
    /**
     * The lager the number the slower the spreading speed. Belongs to [0,+inf)
     */
    var spreadingSpeed: Int = 1000
    /**
     * The maximum number of a virus can produce. -1 means unlimited.
     */
    var maxReproductionScale: Int = -1
    var inheritChildrenNumber: Boolean = false
    /**
     * The maximum number of the generation of the ZERO virus can produce. -1 means unlimited.
     */
    var maxGeneration: Int = -1
    var mutationRate: Int = 1
    /**
     * The maximum number of the mutation of the ZERO virus can produce. -1 means unlimited.
     */
    var maxMutationNumber: Int = -1
    var canMutate: Boolean = false
    var startMutationPercent: Float = 0.8f
    lateinit var raceMaskTR: TextureRegion

    init {
        solid = true
        update = true
        canOverdrive = true
        size = 1
    }

    override fun load() {
        super.load()
        raceMaskTR = this.subA("race-mask")
    }

    open val maxGenerationOrDefault: Int
        get() = if (maxGeneration == -1) 100 else maxGeneration

    override fun setBars() {
        super.setBars()
        bars.add<VirusBuild>(R.Bar.GenerationN) {
            Bar(
                { R.Bar.Generation.bundle(it.curGeneration) },
                { R.C.VirusBK },
                { it.curGeneration / maxGenerationOrDefault.toFloat() }
            )
        }
        DebugOnly {
            bars.add<VirusBuild>(R.Bar.IsAliveN) {
                Bar(
                    { R.Bar.IsAlive.bundle(it.isAlive) },
                    { R.C.IsAive },
                    { if (it.isAlive) 1f else 0f }
                )
            }
            bars.add<VirusBuild>(R.Bar.NeighborStateN) {
                Bar(
                    { "${it.neighborState.countOneBits()}" },
                    { Pal.bar },
                    { it.neighborState.countOneBits() / 8f }
                )
            }
            bars.addSleepInfo()
        }
    }

    override fun minimapColor(tile: Tile) = R.C.VirusBK.rgba()
    open inner class VirusBuild : Building(), IVirusBuilding {
        var neighborState: Int = 0
        var curGeneration: Int = 0
        var curChildrenNumber: Int = 0
        var curVarianceNumber: Int = 0
        var isAlive: Boolean = true
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
            if (ShaderRegistry.test != null) {
                Draw.draw(Layer.block) {
                    Draw.shader(ShaderRegistry.test)
                    Draw.rect(block.region, x, y)
                    if (raceColor != null) {
                        Draw.color(raceColor)
                        Draw.rect(raceMaskTR, x, y)
                        Draw.color()
                    }
                    Draw.shader()
                    Draw.reset()
                }
            }
            drawTeamTop()
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