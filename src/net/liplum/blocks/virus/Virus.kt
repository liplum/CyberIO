package net.liplum.blocks.virus

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.TextureRegion
import arc.math.Mathf
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.Vars
import mindustry.gen.Building
import mindustry.ui.Bar
import mindustry.world.Tile
import net.liplum.R
import net.liplum.api.virus.UninfectedBlocksRegistry
import net.liplum.blocks.AnimedBlock
import net.liplum.utils.VirusUtil
import net.liplum.utils.bundle
import net.liplum.utils.subA

typealias UBR = UninfectedBlocksRegistry

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
    lateinit var raceMaskTR: TextureRegion;

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
        bars.add<VirusBuild>(R.Bar.GenerationName) {
            Bar(
                { R.Bar.Generation.bundle(it.curGeneration) },
                { R.C.VirusBK },
                { it.curGeneration / maxGenerationOrDefault.toFloat() }
            )
        }
    }

    override fun minimapColor(tile: Tile) = R.C.VirusBK.rgba()
    open inner class VirusBuild : Building(), IVirusBuilding {
        var curGeneration: Int = 0
        var curChildrenNumber: Int = 0
        var curVarianceNumber: Int = 0
        var isAlive: Boolean = true
        var raceColor: Color? = null
        override fun updateTile() {
            if (isDead) {
                return
            }
            if (selfOnUninfectedFloorOrOverLay) {
                setDead()
            }
            if (isDead) {
                return
            }
            if (canReproduce) {
                var speed = spreadingSpeed
                if (canOverdrive) {
                    speed = (speed / timeScale).toInt()
                }
                val luckyNumber = Mathf.random(speed)
                if (luckyNumber == 0) {
                    val randomDX = Mathf.random(-1, 1)
                    val randomDY = Mathf.random(-1, 1)
                    val selfX = tile.x.toInt()
                    val selfY = tile.y.toInt()
                    val infected = Vars.world.tile(selfX + randomDX, selfY + randomDY)
                    if (VirusUtil.canInfect(infected)) {
                        this.reproduce(infected)
                    }
                }
            }
        }

        override fun draw() {
            Draw.rect(block.region, x, y)
            if (raceColor != null) {
                Draw.color(raceColor)
                Draw.rect(raceMaskTR, x, y)
                Draw.color()
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
            infected.setBlock(this@Virus, team)
            val newGen = infected.build as? VirusBuild
            newGen?.curGeneration = this.curGeneration + 1
            curChildrenNumber++
            if (inheritChildrenNumber) {
                newGen?.curChildrenNumber = this.curChildrenNumber
            }
            newGen?.raceColor = this.raceColor
        }

        open fun reproduceVariant(infected: Tile) {
            infected.setBlock(this@Virus, team)
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
                tile.setAir()
            }
        }

        override fun write(write: Writes) {
            super.write(write)
            write.s(curGeneration)
            write.s(curChildrenNumber)
            write.s(curVarianceNumber)
            write.i(raceColor?.rgba() ?: -1)
            write.bool(isAlive)
        }

        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            curGeneration = read.s().toInt()
            curChildrenNumber = read.s().toInt()
            curVarianceNumber = read.s().toInt()
            val raceColorNumber = read.i()
            raceColor = if (raceColorNumber == -1) null else Color(raceColorNumber)
            isAlive = read.bool()
        }

        override fun killVirus() {
            setDead()
        }

        override val isDead: Boolean
            get() = !isAlive
    }
}