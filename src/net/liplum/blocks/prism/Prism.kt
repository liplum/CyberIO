package net.liplum.blocks.prism

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.math.Angles
import arc.math.Mathf
import arc.struct.EnumSet
import arc.struct.Seq
import arc.util.Time
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.Vars
import mindustry.content.UnitTypes
import mindustry.entities.TargetPriority
import mindustry.gen.BlockUnitc
import mindustry.gen.Building
import mindustry.gen.Groups
import mindustry.gen.Unit
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import mindustry.ui.Bar
import mindustry.world.Block
import mindustry.world.blocks.ControlBlock
import mindustry.world.meta.BlockFlag
import mindustry.world.meta.BlockGroup
import net.liplum.ClientOnly
import net.liplum.DebugOnly
import net.liplum.R
import net.liplum.blocks.prism.TintedBullets.Companion.isTintIgnored
import net.liplum.blocks.prism.TintedBullets.Companion.tintedRGB
import net.liplum.draw
import net.liplum.math.PolarPos
import net.liplum.persistance.readSeq
import net.liplum.persistance.writeSeq
import net.liplum.utils.*
import kotlin.math.abs

enum class PrismData {
    Duplicate
}
private typealias Obelisk = PrismObelisk.ObeliskBuild

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
    @JvmField var maxPrisel = 3
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

    open val Prisel.color: Color
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
                    if (it.actualPriselCount > 1)
                        "${it.actualPriselCount} ${R.Bar.PrismPl.bundle()}"
                    else
                        "${it.actualPriselCount} ${R.Bar.Prism.bundle()}"
                },
                {
                    val rgb = R.C.PrismRgbFG
                    val len = rgb.size
                    val total = len * 60f
                    rgb[((Time.time % total / total) * len).toInt().coerceIn(0, len - 1)]
                },
                { it.actualPriselCount.toFloat() / maxPrisel }
            )
        }
    }

    open inner class PrismBuild : Building(), ControlBlock {
        lateinit var crystalImg: TR
        var prisels: Seq<Prisel> = Seq(maxPrisel)
        var unit = UnitTypes.block.create(team) as BlockUnitc
        var obelisks: Seq<Int> = Seq()
        var actualPriselCount = 0

        init {
            addNewPriselToOutermost()
        }

        override fun unit(): Unit {
            unit.tile(this)
            unit.team(team)
            return (unit as Unit)
        }

        override fun created() {
            super.created()
            ClientOnly {
                crystalImg = CrystalTRs[Mathf.random(0, CrystalTRs.size - 1)]
            }
        }

        override fun canControl() = playerControllable
        fun addNewPriselToOutermost() {
            val count = prisels.size
            if (count < maxPrisel) {
                val newOne = Prisel().apply {
                    revolution = PolarPos(0f, 0f)
                    rotation = PolarPos(prismRange, 0f)
                    isClockwise = count % 2 != 0
                }
                prisels.addPrisel(newOne)
            }
        }

        fun removeOutermostPrisel() {
            if (prisels.size > 0) {
                var last: Prisel
                for (i in prisels.size - 1 downTo 0) {
                    last = prisels[i]
                    if (!last.isRemoved) {
                        last.isRemoved = true
                        actualPriselCount--
                        break
                    }
                }
            }
        }

        fun Seq<Prisel>.addPrisel(prisel: Prisel) {
            this.add(prisel)
            actualPriselCount++
        }

        override fun onProximityUpdate() {
            super.onProximityUpdate()
            var myObeliskNumber = 0
            for (b in proximity) {
                if (b is Obelisk && b.prismType == this@Prism) {
                    if (b.linked == null) {
                        if (obelisks.size < maxPrisel) {
                            link(b)
                            myObeliskNumber++
                        }
                    } else if (b.linked == this) {
                        myObeliskNumber++
                    }
                }
            }
            obelisks.remove {
                val b = Vars.world.tile(it).build
                !(b is Obelisk && b.prismType == this@Prism && b.linked == this)
            }
            if (actualPriselCount > 1) {
                // If any obelisk was removed, then remove any prisel until their numbers are equal.
                var needRemoved = actualPriselCount - myObeliskNumber - 1
                while (needRemoved > 0) {
                    removeOutermostPrisel()
                    needRemoved--
                }
            }
        }

        fun link(obelisk: Obelisk) {
            if (obelisks.size < maxPrisel) {
                obelisk.linked = this
                obelisks.add(obelisk.pos())
                addNewPriselToOutermost()
            }
        }

        override fun updateTile() {
            val perRevl = prismRevolutionSpeed * delta()
            val perRota = prismRotationSpeed * delta()
            val ta = PolarPos.toA(unit.aimX() - x, unit.aimY() - y)
            var curPerRelv: Float
            var curPerRota: Float

            for ((i, prisel) in prisels.withIndex()) {
                val ip1 = i + 1
                curPerRelv = perRevl / ip1 * 0.8f
                curPerRota = perRota * ip1 * 0.8f
                if (isControlled) {
                    prisel.revolution.a =
                        Angles.moveToward(
                            prisel.revolution.a.degree, ta.degree,
                            curPerRelv * 100 * delta()
                        ).radian
                } else {
                    prisel.revolution.a += if (prisel.isClockwise) -curPerRelv else curPerRelv
                }
                var revlR = prisel.revolution.r
                var perRevlR = 0.1f * Mathf.log2((ip1 * ip1).toFloat() + 1f)
                val totalLen = Agl + (prismRange * 2 * i)
                if (prisel.isRemoved) {
                    perRevlR = PSA(perRevlR, revlR, 0f, totalLen)
                    revlR -= perRevlR
                } else {
                    perRevlR = PSA(perRevlR, revlR, totalLen, totalLen)
                    revlR += perRevlR
                }
                revlR = revlR.coerceAtMost(totalLen)
                prisel.revolution.r = revlR
                prisel.rotation.a += if (prisel.isClockwise) -curPerRota else curPerRota
            }
            prisels.remove { it.isRemoved && it.revolution.r <= 0f }
            var priselX: Float
            var priselY: Float
            val realRange = Agl + (prismRange * 2 * prisels.size)
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
            for ((i, prisel) in prisels.withIndex()) {
                priselX = prisel.revolution.toX() + x
                priselY = prisel.revolution.toY() + y
                Draw.z(Layer.blockOver)
                Drawf.shadow(
                    crystalImg,
                    priselX - elevation * Mathf.log(3f, i + 3f) * 7f,
                    priselY - elevation * Mathf.log(3f, i + 3f) * 7f,
                    prisel.rotation.a.degree.draw
                )
                Draw.z(Layer.turret)
                Draw.rect(
                    crystalImg,
                    priselX,
                    priselY,
                    prisel.rotation.a.degree.draw
                )

                DebugOnly {
                    Draw.z(Layer.overlayUI)
                    Drawf.circles(priselX, priselY, prismRange, prisel.color)
                }
            }
            Draw.reset()
        }

        override fun drawSelect() {
            G.init()
            Draw.z(Layer.blockUnder)
            for ((i, prisel) in prisels.withIndex()) {
                G.drawDashCircle(
                    this,
                    Agl + (prismRange * 2 * i),
                    prisel.color
                )
            }
        }

        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            prisels = read.readSeq(Prisel::read)
            obelisks = read.readSeq(Reads::i)
            actualPriselCount = read.b().toInt()
        }

        override fun write(write: Writes) {
            super.write(write)
            write.writeSeq(prisels, Prisel::write)
            write.writeSeq(obelisks, Writes::i)
            write.b(actualPriselCount)
        }
    }
}