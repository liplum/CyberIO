package net.liplum.blocks.prism

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.math.Angles
import arc.math.Mathf
import arc.struct.EnumSet
import arc.util.Time
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.content.UnitTypes
import mindustry.entities.TargetPriority
import mindustry.gen.BlockUnitc
import mindustry.gen.Building
import mindustry.gen.Groups
import mindustry.gen.Unit
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import mindustry.graphics.Pal
import mindustry.ui.Bar
import mindustry.world.Block
import mindustry.world.blocks.ControlBlock
import mindustry.world.meta.BlockFlag
import mindustry.world.meta.BlockGroup
import net.liplum.ClientOnly
import net.liplum.DebugOnly
import net.liplum.R
import net.liplum.blocks.prism.CrystalManager.Companion.read
import net.liplum.blocks.prism.CrystalManager.Companion.write
import net.liplum.blocks.prism.TintedBullets.Companion.isTintIgnored
import net.liplum.blocks.prism.TintedBullets.Companion.tintedRGB
import net.liplum.draw
import net.liplum.math.PolarPos
import net.liplum.utils.*
import kotlin.math.abs

enum class PrismData {
    Duplicate
}

open class Prism(name: String) : Block(name) {
    var PS: FUNC = quadratic(1.2f, 0.2f)
    /**
     * Above ground level.
     */
    @JvmField var Agl = 20f
    @JvmField var deflectionAngle = 25f
    @JvmField var prismRange = 10f
    @JvmField var prismRevolutionSpeed = 0.05f
    @JvmField var playerControllable = true
    @JvmField var rotateSpeed = 0f
    @JvmField var maxCrystal = 3
    @ClientOnly @JvmField var prismRotationSpeed = 0.05f
    @ClientOnly @JvmField var elevation = -1f
    @ClientOnly lateinit var BaseTR: TR
    @ClientOnly lateinit var CrystalTRs: Array<TR>
    @ClientOnly @JvmField var CrystalVariants = 7
    @ClientOnly @JvmField var clockwiseColor: Color = R.C.prismClockwise
    @ClientOnly @JvmField var antiClockwiseColor: Color = R.C.prismAntiClockwise
    @JvmField var tintBullet = true

    init {
        absorbLasers = true
        update = true
        solid = true
        outlineIcon = true
        priority = TargetPriority.turret
        group = BlockGroup.turrets
        flags = EnumSet.of(BlockFlag.turret)
    }

    open val Crystal.color: Color
        get() = if (this.isClockwise)
            clockwiseColor
        else
            antiClockwiseColor

    fun PSA(speed: Float, cur: Float, target: Float, totalLength: Float): Float {
        return speed * PS(abs(target - cur) / totalLength)
    }

    override fun load() {
        super.load()
        BaseTR = region
        CrystalTRs = this.sheet("crystals", CrystalVariants)
    }

    var perDeflectionAngle = 0f
    override fun init() {
        super.init()
        perDeflectionAngle = deflectionAngle * 2 / 3
        if (elevation < 0) {
            elevation = size / 2f
        }
    }

    override fun setBars() {
        super.setBars()
        bars.add<PrismBuild>(R.Bar.PrismN) {
            Bar(
                {
                    if (it.cm.validAmount > 1)
                        "${it.crystalAmount} ${R.Bar.PrismPl.bundle()}"
                    else
                        "${it.crystalAmount} ${R.Bar.Prism.bundle()}"
                },
                {
                    val rgb = R.C.PrismRgbFG
                    val len = rgb.size
                    val total = len * 60f
                    rgb[((Time.time % total / total) * len).toInt().coerceIn(0, len - 1)]
                },
                { it.crystalAmount.toFloat() / maxCrystal }
            )
        }

        bars.add<PrismBuild>(R.Bar.ProgressN) {
            Bar(
                { R.Bar.Progress.bundle(it.cm.process) },
                { Pal.power },
                { it.cm.process / 1f }
            )
        }
        bars.add<PrismBuild>(R.Bar.StatusN) {
            Bar(
                { it.cm.status.toString() },
                { Pal.accent },
                { 1f }
            )
        }
    }

    open inner class PrismBuild : Building(), ControlBlock {
        @ClientOnly lateinit var crystalImg: TR
        var cm: CrystalManager = CrystalManager().apply { prism = this@PrismBuild }
        var unit = UnitTypes.block.create(team) as BlockUnitc
        override fun canControl() = playerControllable
        val crystalAmount: Int
            get() = cm.validAmount

        override fun created() {
            super.created()
            ClientOnly {
                crystalImg = CrystalTRs[Mathf.random(0, CrystalTRs.size - 1)]
            }
            if (cm.validAmount == 0) {
                addNewPriselToOutermost()
            }
        }

        fun addNewPriselToOutermost() {
            cm.tryAddNew {
                revolution = PolarPos(0f, 0f)
                rotation = PolarPos(prismRange, 0f)
                isClockwise = orbitPos % 2 != 0
            }
        }

        fun removeOutermostPrisel() =
            cm.tryRemoveOutermost()

        override fun onRemoved() =
            cm.unlinkAllObelisks()

        override fun onProximityUpdate() {
            super.onProximityUpdate()
            cm.removeNonexistentObelisk()
            if (cm.canAdd) {
                for (b in proximity) {
                    if (
                        cm.canAdd &&
                        b is Obelisk &&
                        b.prismType == this@Prism &&
                        b.linked == null
                    ) {
                        tryLink(b)
                    }
                }
            }
            cm.updateObelisk()
        }

        fun tryLink(obelisk: Obelisk) {
            if (cm.tryLink(obelisk)) {
                obelisk.linked = this
                addNewPriselToOutermost()
            }
        }

        override fun updateTile() {
            cm.spend(delta())
            val perRevl = prismRevolutionSpeed * delta()
            val perRota = prismRotationSpeed * delta()
            val ta = PolarPos.toA(unit.aimX() - x, unit.aimY() - y)
            var curPerRelv: Float
            var curPerRota: Float

            cm.update {
                val ip1 = orbitPos + 1
                curPerRelv = perRevl / ip1 * 0.8f
                curPerRota = perRota * ip1 * 0.8f
                if (isControlled) {
                    revolution.a =
                        Angles.moveToward(
                            revolution.a.degree, ta.degree,
                            curPerRelv * 100 * delta()
                        ).radian
                } else {
                    revolution.a += if (isClockwise) -curPerRelv else curPerRelv
                }
                var revlR = revolution.r
                var perRevlR = 0.1f * Mathf.log2((ip1 * ip1).toFloat() + 1f)
                val totalLen = Agl + (prismRange * 2 * orbitPos)
                if (isRemoved) {
                    perRevlR = PSA(perRevlR, revlR, 0f, totalLen)
                    revlR -= perRevlR
                } else {
                    perRevlR = PSA(perRevlR, revlR, totalLen, totalLen)
                    revlR += perRevlR
                }
                revlR = revlR.coerceIn(0f, totalLen)
                revolution.r = revlR
                rotation.a += if (isClockwise) -curPerRota else curPerRota
            }
            var priselX: Float
            var priselY: Float
            val realRange = Agl + (prismRange * 2 * crystalAmount)
            val realRangeHalf = realRange / 2
            Groups.bullet.intersect(
                x - realRangeHalf,
                y - realRangeHalf,
                realRange,
                realRange
            ) {
                cm.update {
                    priselX = revolution.toX() + x
                    priselY = revolution.toY() + y
                    if (it.team == team && Util2D.distance(it.x, it.y, priselX, priselY) < prismRange) {
                        if (it.data != PrismData.Duplicate) {
                            it.data = PrismData.Duplicate
                            val angle = it.rotation()
                            val copyRed = it.copy()
                            val copyBlue = it.copy()
                            val start = angle - deflectionAngle
                            copyRed.rotation(start)
                            it.rotation(start + perDeflectionAngle)
                            copyBlue.rotation(start + perDeflectionAngle * 2)
                            if (tintBullet) {
                                if (!it.type.isTintIgnored) {
                                    val rgbs = tintedRGB(it.type)
                                    copyRed.type = rgbs[0]
                                    it.type = rgbs[1]
                                    copyBlue.type = rgbs[2]
                                }
                            }
                        }
                    }
                }
            }
        }

        override fun draw() {
            Draw.rect(BaseTR, x, y)
            var priselX: Float
            var priselY: Float
            cm.render {
                priselX = revolution.toX() + x
                priselY = revolution.toY() + y
                Draw.z(Layer.blockOver)
                Drawf.shadow(
                    crystalImg,
                    priselX - elevation * Mathf.log(3f, orbitPos + 3f) * 7f,
                    priselY - elevation * Mathf.log(3f, orbitPos + 3f) * 7f,
                    rotation.a.degree.draw
                )
                Draw.z(Layer.turret)
                Draw.rect(
                    crystalImg,
                    priselX,
                    priselY,
                    rotation.a.degree.draw
                )

                DebugOnly {
                    Draw.z(Layer.overlayUI)
                    Drawf.circles(priselX, priselY, prismRange, color)
                }
            }
            Draw.reset()
        }

        override fun drawSelect() {
            G.init()
            Draw.z(Layer.blockUnder)
            cm.render {
                G.drawDashCircle(
                    this@PrismBuild,
                    Agl + (prismRange * 2 * orbitPos),
                    color
                )
            }
        }

        override fun unit(): Unit {
            unit.tile(this)
            unit.team(team)
            return (unit as Unit)
        }

        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            cm = read.read().apply { prism = this@PrismBuild }
        }

        override fun write(write: Writes) {
            super.write(write)
            write.write(cm)
        }
    }
}