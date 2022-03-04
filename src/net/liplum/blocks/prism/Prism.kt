package net.liplum.blocks.prism

import arc.Core
import arc.graphics.g2d.Draw
import arc.math.Angles
import arc.math.Mathf
import arc.struct.EnumSet
import arc.struct.Seq
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.content.UnitTypes
import mindustry.entities.TargetPriority
import mindustry.game.Team
import mindustry.gen.BlockUnitc
import mindustry.gen.Building
import mindustry.gen.Groups
import mindustry.gen.Unit
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import mindustry.world.Block
import mindustry.world.Tile
import mindustry.world.blocks.ControlBlock
import mindustry.world.meta.BlockFlag
import mindustry.world.meta.BlockGroup
import net.liplum.ClientOnly
import net.liplum.DebugOnly
import net.liplum.animations.anims.Animation
import net.liplum.blocks.prism.TintedBullets.Companion.tintBulletRGB
import net.liplum.draw
import net.liplum.math.PolarPos
import net.liplum.utils.*

enum class PrismData {
    Duplicate
}

private const val MaxPrisel = 3

open class Prism(name: String) : Block(name) {
    lateinit var PrismAnim: Animation
    /**
     * Above ground level.
     */
    @JvmField var Agl = 20f
    @JvmField var deflectionAngle = 25f
    @JvmField var prismRange = 10f
    @JvmField var prismRevolutionSpeed = 0.05f
    @JvmField @ClientOnly var prismRotationSpeed = 0.05f
    @JvmField var playerControllable = true
    @JvmField var rotateSpeed = 0f
    lateinit var BaseTR: TR
    lateinit var PriselTR: TR
    var elevation = -1f

    init {
        absorbLasers = true
        update = true
        solid = true
        outlineIcon = true
        priority = TargetPriority.turret
        group = BlockGroup.turrets
        flags = EnumSet.of(BlockFlag.turret)
    }

    override fun load() {
        super.load()
        PrismAnim = this.autoAnim(frame = 7, totalDuration = 60f)
        BaseTR = Core.atlas.find("turret-base")
        PriselTR = PrismAnim[0]
    }

    var perDeflectionAngle = 0f
    override fun init() {
        super.init()
        perDeflectionAngle = deflectionAngle * 2 / 3
        if (elevation < 0) {
            elevation = size / 2f
        }
    }

    override fun canPlaceOn(tile: Tile, team: Team, rotation: Int): Boolean {
        return super.canPlaceOn(tile, team, rotation) ||
                (tile.block() == this && tile.team() == team)
    }

    override fun canReplace(other: Block): Boolean {
        return super.canReplace(other) || other == this
    }

    open inner class PrismBuild : Building(), ControlBlock {
        var prisels: Seq<Prisel> = Seq(MaxPrisel)
        val curPriselCount: Int
            get() = prisels.size + 1
        var unit = UnitTypes.block.create(team) as BlockUnitc

        init {
            addNewPrisel()
            addNewPrisel()
        }

        override fun unit(): Unit {
            //make sure stats are correct
            unit.tile(this)
            unit.team(team)
            return (unit as Unit)
        }

        override fun canControl() = playerControllable
        fun addNewPrisel() {
            val count = prisels.size
            if (count < MaxPrisel) {
                prisels.add(Prisel().apply {
                    revolution = PolarPos(Agl + (prismRange * 2 * count), 0f)
                    rotation = PolarPos(prismRange, 0f)
                    isClockwise = count % 2 != 0
                })
            }
        }

        fun updatePrisel(priselNumber: Int) {
            if (priselNumber > curPriselCount) {
                for (i in 0 until priselNumber - curPriselCount) {
                    addNewPrisel()
                }
            }
        }

        override fun updateTile() {
            val perRelv = prismRevolutionSpeed * delta()
            val perRota = prismRotationSpeed * delta()
            val ta = PolarPos.toA(unit.aimX() - x, unit.aimY() - y)
            var curPerRelv: Float
            var curPerRota: Float

            for ((i, prisel) in prisels.withIndex()) {
                curPerRelv = perRelv / (i + 1) * 0.8f
                curPerRota = perRota * (i + 1) * 0.8f
                if (isControlled) {
                    prisel.revolution.a =
                        Angles.moveToward(
                            prisel.revolution.a.degree, ta.degree,
                            curPerRelv * 100 * delta()
                        ).radian
                } else {
                    prisel.revolution.a += if (prisel.isClockwise) -curPerRelv else curPerRelv
                }
                prisel.rotation.a += if (prisel.isClockwise) -curPerRota else curPerRota
            }
            var priselX: Float
            var priselY: Float
            val realRange = Agl + +(prismRange * 2 * curPriselCount)
            val realRangeHalf = realRange / 2
            Groups.bullet.intersect(
                x - realRangeHalf,
                y - realRangeHalf,
                realRange,
                realRange
            ) {
                for (prisel in prisels) {
                    priselX = prisel.revolution.toX() + x
                    priselY = prisel.revolution.toY() + y
                    if (it.team == team && Util2D.distance(it.x, it.y, priselX, priselY) < prismRange) {
                        if (it.data != PrismData.Duplicate) {
                            it.data = PrismData.Duplicate
                            val angle = it.rotation()
                            val copyGreen = it.copy()
                            val copyBlue = it.copy()
                            val start = angle - deflectionAngle
                            it.rotation(start)
                            copyGreen.rotation(start + perDeflectionAngle)
                            copyBlue.rotation(start + perDeflectionAngle * 2)
                            tintBulletRGB(it, copyGreen, copyBlue)
                        }
                    }
                }
            }
        }

        override fun draw() {
            Draw.rect(BaseTR, x, y)
            var priselX: Float
            var priselY: Float
            for ((i, prisel) in prisels.withIndex()) {
                priselX = prisel.revolution.toX() + x
                priselY = prisel.revolution.toY() + y
                Draw.z(Layer.blockOver)
                Drawf.shadow(
                    PriselTR,
                    priselX - elevation * Mathf.log(3f, i + 3f) * 7f,
                    priselY - elevation * Mathf.log(3f, i + 3f) * 7f,
                    prisel.rotation.a.degree.draw
                )
                Draw.z(Layer.turret)
                Draw.rect(
                    PriselTR,
                    priselX,
                    priselY,
                    prisel.rotation.a.degree.draw
                )

                DebugOnly {
                    Draw.z(Layer.overlayUI)
                    Drawf.circles(priselX, priselY, prismRange)
                }
            }
            Draw.reset()
        }

        override fun drawSelect() {
            G.init()
            Draw.z(Layer.blockUnder)
            for (count in 0 until curPriselCount - 1) {
                G.drawDashCircle(
                    this,
                    Agl + (prismRange * 2 * count),
                    team.color
                )
            }
        }

        override fun overwrote(previous: Seq<Building>) {
            for (origin in previous) {
                if (origin is PrismBuild) {
                    this.updatePrisel(origin.curPriselCount + 1)
                }
            }
        }

        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            // prisels = read.readSeq(Prisel::read)
        }

        override fun write(write: Writes) {
            super.write(write)
            // write.writeSeq(prisels, Prisel::write)
        }
    }
}