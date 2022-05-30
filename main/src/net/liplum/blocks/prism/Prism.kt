package net.liplum.blocks.prism

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.math.Angles
import arc.math.Interp
import arc.math.Mathf
import arc.struct.EnumSet
import arc.struct.Seq
import arc.util.Time
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.Vars.tilesize
import mindustry.content.UnitTypes
import mindustry.entities.TargetPriority
import mindustry.gen.*
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import mindustry.graphics.Pal
import mindustry.logic.LAccess
import mindustry.logic.Ranged
import mindustry.world.Block
import mindustry.world.blocks.ControlBlock
import mindustry.world.blocks.defense.turrets.ContinuousTurret
import mindustry.world.blocks.defense.turrets.LaserTurret
import mindustry.world.blocks.defense.turrets.Turret
import mindustry.world.meta.BlockFlag
import mindustry.world.meta.BlockGroup
import net.liplum.DebugOnly
import net.liplum.R
import net.liplum.api.prism.PrismBlackList.canDisperse
import net.liplum.api.prism.PrismRegistry.isDuplicate
import net.liplum.api.prism.PrismRegistry.setDuplicate
import net.liplum.blocks.prism.CrystalManager.Companion.read
import net.liplum.blocks.prism.CrystalManager.Companion.write
import net.liplum.lib.Serialized
import net.liplum.lib.TR
import net.liplum.lib.TRs
import net.liplum.lib.math.*
import net.liplum.lib.utils.bundle
import net.liplum.lib.utils.invoke
import net.liplum.lib.utils.isZero
import net.liplum.lib.utils.percentI
import net.liplum.mdt.ClientOnly
import net.liplum.mdt.advanced.Inspector
import net.liplum.mdt.advanced.Inspector.isSelectedByMouse
import net.liplum.mdt.mixin.copy
import net.liplum.mdt.render.AsShadow
import net.liplum.mdt.render.DrawSize
import net.liplum.mdt.render.G
import net.liplum.mdt.ui.bars.AddBar
import net.liplum.mdt.utils.*
import kotlin.math.abs
import kotlin.math.log2

open class Prism(name: String) : Block(name) {
    var PS: FUNC = quadratic(1.2f, 0.2f)
    /**
     * Above ground level.
     */
    @JvmField var Agl = 20f
    @JvmField var deflectionAngle = 25f
    @JvmField var prismRadius = 10.5f
    @JvmField var prismRevolutionSpeed = 0.05f
    @JvmField var playerControllable = true
    @JvmField var rotateSpeed = 0f
    @JvmField var maxCrystal = 3
    @JvmField var tintBullet = true
    @ClientOnly @JvmField var prismRotationSpeed = 0.05f
    @ClientOnly @JvmField var elevation = -1f
    @ClientOnly @JvmField var CrystalVariants = 7
    @ClientOnly @JvmField var sizeOpen = 15f
    @ClientOnly @JvmField var elevationHeightScale = 0.2f
    @ClientOnly @JvmField var maxOutsideRange = -1f
    @ClientOnly @JvmField var maxOutsideRangeFactor = 0.8f
    @ClientOnly @JvmField var expendingSelectCircleTime = 45f
    @ClientOnly lateinit var BaseTR: TR
    @ClientOnly lateinit var CrystalTRs: TRs
    @ClientOnly lateinit var UpTR: TR
    @ClientOnly lateinit var DownTR: TR
    @ClientOnly lateinit var LeftTR: TR
    @ClientOnly lateinit var RightTR: TR
    @ClientOnly lateinit var RightUpStartTR: TR
    @ClientOnly lateinit var RightUpEndTR: TR
    @ClientOnly lateinit var LeftDownStartTR: TR
    @ClientOnly lateinit var LeftDownEndTR: TR

    init {
        updateInUnits = true
        alwaysUpdateInUnits = true
        absorbLasers = true
        update = true
        solid = true
        outlineIcon = true
        priority = TargetPriority.turret
        group = BlockGroup.turrets
        flags = EnumSet.of(BlockFlag.turret)
        noUpdateDisabled = true
        sync = true
    }

    open val Crystal.circleColor: Color
        get() = R.C.PrismRgbFG[orbitPos % 3]

    fun PSA(speed: Float, cur: Float, target: Float, totalLength: Float): Float {
        return speed * PS(abs(target - cur) / totalLength)
    }

    override fun load() {
        super.load()
        BaseTR = this.sub("base")
        CrystalTRs = this.sheet("crystals", CrystalVariants)
        UpTR = this.sub("up")
        LeftTR = this.sub("left")
        DownTR = this.sub("down")
        RightTR = this.sub("right")
        RightUpStartTR = this.sub("rightup-start")
        RightUpEndTR = this.sub("rightup-end")
        LeftDownStartTR = this.sub("leftdown-start")
        LeftDownEndTR = this.sub("leftdown-end")
    }

    var perDeflectionAngle = 0f
    override fun init() {
        super.init()
        perDeflectionAngle = deflectionAngle * 2 / 3
        if (elevation < 0f)
            elevation = size / 2f
        if (maxOutsideRange < 0f)
            maxOutsideRange = (size * tilesize * maxOutsideRangeFactor).coerceAtMost(prismRadius - 1f)
        clipSize = Agl + (prismRadius * 3 * maxCrystal) + elevation
    }

    override fun setBars() {
        super.setBars()
        AddBar<PrismBuild>(R.Bar.PrismN,
            {
                if (cm.validAmount > 1)
                    "$crystalAmount ${R.Bar.PrismPl.bundle()}"
                else
                    "$crystalAmount ${R.Bar.Prism.bundle()}"
            }, AutoRGBx,
            { crystalAmount.toFloat() / maxCrystal }
        )
        DebugOnly {
            AddBar<PrismBuild>(R.Bar.ProgressN,
                { R.Bar.Progress.bundle(cm.process.percentI) },
                { Pal.power },
                { cm.process / 1f }
            )
            AddBar<PrismBuild>(R.Bar.StatusN,
                { cm.status.toString() },
                { Pal.accent },
                { 1f }
            )
            AddBar<PrismBuild>("obelisk-count",
                { "Obelisk:${cm.obeliskCount}" },
                { Pal.accent },
                { cm.obeliskCount.toFloat() / (maxCrystal - 1) }
            )
        }
    }

    open inner class PrismBuild : Building(),
        ControlBlock, Ranged {
        @Serialized
        @JvmField var cm: CrystalManager = CrystalManager().apply {
            maxAmount = maxCrystal
            prism = this@PrismBuild
            ClientOnly {
                genCrystalImgCallback = {
                    img = CrystalTRs.randomOne()
                }
            }
            addCrystalCallback = {
                revolution = Polar(0f, 0f)
                rotation = Polar(prismRadius, 0f)
                isClockwise = orbitPos % 2 != 0
            }
            for (i in 0 until initCrystalCount) {
                tryAddNew()
            }
        }
        var unit = UnitTypes.block.create(team) as BlockUnitc
        override fun canControl() = playerControllable
        val crystalAmount: Int
            get() = cm.validAmount
        val outer: Prism
            get() = this@Prism
        var logicControlTime = -1f
        val controlByLogic: Boolean
            get() = logicControlTime > 0f
        var logicAngle = 0f
        val realRange: Float
            get() = Agl + (prismRadius * 2 * crystalAmount)

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
                        b.canLink(this)
                    ) {
                        cm.tryLink(b)
                    }
                }
            }
            cm.updateObelisk()
        }

        override fun delta(): Float {
            return Time.delta * log2(timeScale + 1f)
        }

        override fun updateTile() {
            val delta = delta()
            cm.spend(delta)
            if (logicControlTime > 0f) {
                logicControlTime -= Time.delta
            }
            val perRevl = prismRevolutionSpeed * delta
            val perRota = prismRotationSpeed * delta

            cm.update {
                val ip1 = orbitPos + 1
                val curPerRelv = perRevl / ip1 * 0.8f
                val curPerRota = perRota * Mathf.log(2.5f, orbitPos + 2.5f)
                if (isControlled) {
                    val ta = Polar.toA(unit.aimX() - x, unit.aimY() - y)
                    revolution.a =
                        Angles.moveToward(
                            revolution.a.degree, ta.degree,
                            curPerRelv * 100 * delta
                        ).radian
                } else if (controlByLogic) {
                    revolution.a =
                        Angles.moveToward(
                            revolution.a.degree, logicAngle,
                            curPerRelv * 100 * delta
                        ).radian
                } else {
                    revolution.a += if (isClockwise) -curPerRelv else curPerRelv
                }
                var revlR = revolution.r
                var perRevlR = 0.1f * Mathf.log2((ip1 * ip1).toFloat() + 1f)
                val totalLen = Agl + (prismRadius * 2 * orbitPos)
                if (isRemoved) {
                    perRevlR = PSA(perRevlR, revlR, 0f, totalLen)
                    revlR -= perRevlR * delta
                } else {
                    perRevlR = PSA(perRevlR, revlR, totalLen, totalLen)
                    revlR += perRevlR * delta
                }
                revlR = revlR.coerceIn(0f, totalLen)
                revolution.r = revlR
                rotation.a += if (isClockwise) -curPerRota * delta
                else curPerRota * delta
            }
            var priselX: Float
            var priselY: Float
            val realRange = realRange
            val realRangeX2 = realRange * 2
            Groups.bullet.intersect(
                x - realRange,
                y - realRange,
                realRangeX2,
                realRangeX2
            ) {
                cm.update {
                    priselX = revolution.x + x
                    priselY = revolution.y + y
                    if (it.team == team &&
                        !it.data.isDuplicate &&
                        it.dst(priselX, priselY) <= prismRadius
                    ) {
                        it.passThrough()
                    }
                }
            }
        }
        /**
         * The Current is considered as green.
         */
        open fun Bullet.passThrough() {
            if (!type.canDisperse) return
            this.setDuplicate()
            // Current is green
            val angle = this.rotation()
            val start = angle
            val copyRed = this.copy()
            val copyBlue = this.copy()
            copyRed.rotation(start - perDeflectionAngle)
            this.rotation(start)
            copyBlue.rotation(start + perDeflectionAngle)

            copyRed.handleDuplicate(-perDeflectionAngle)
            this.handleDuplicate(0f)
            copyBlue.handleDuplicate(perDeflectionAngle)
            if (tintBullet) {
                if (!this.type.isTintIgnored) {
                    val rgbs = tintedRGB(this.type)
                    copyRed.type = rgbs[0]
                    this.type = rgbs[1]
                    copyBlue.type = rgbs[2]
                }
            }
        }

        open fun Bullet.handleDuplicate(
            angleOffset: Float = 0f
        ): Bullet {
            fun Seq<Turret.BulletEntry>.copyFirstBulletEntry() {
                if (any()) {
                    val first = first()
                    add(Turret.BulletEntry(this@handleDuplicate, first.x, first.y, first.rotation + angleOffset, first.life))
                }
            }
            when (val owner = owner) {
                is LaserTurret.LaserTurretBuild -> {
                    owner.bullets.copyFirstBulletEntry()
                }
                is ContinuousTurret.ContinuousTurretBuild -> {
                    owner.bullets.copyFirstBulletEntry()
                }
            }
            return this
        }

        override fun draw() {
            Draw.z(Layer.block)
            val isInPayload = inPayload
            Draw.rect(BaseTR, x, y)
            val process = cm.process
            Draw.alpha(1f - process)
            Draw.rect(RightUpStartTR, x, y)
            Draw.rect(RightUpStartTR, x, y, 90f)
            Draw.rect(LeftDownStartTR, x, y)
            Draw.rect(LeftDownStartTR, x, y, -90f)
            Draw.alpha(process)
            Draw.rect(RightUpEndTR, x, y)
            Draw.rect(RightUpEndTR, x, y, 90f)
            Draw.rect(LeftDownEndTR, x, y)
            Draw.rect(LeftDownEndTR, x, y, -90f)
            Draw.color()
            val delta = process * G.D(sizeOpen)

            Draw.z(Layer.blockOver)
            Draw.rect(UpTR, x, y + delta)
            Draw.rect(DownTR, x, y - delta)
            Draw.rect(LeftTR, x - delta, y)
            Draw.rect(RightTR, x + delta, y)

            Drawf.shadow(UpTR, x, y + delta)
            Drawf.shadow(DownTR, x, y - delta)
            Drawf.shadow(LeftTR, x - delta, y)
            Drawf.shadow(RightTR, x + delta, y)
            cm.render {
                val priselX = revolution.x + x
                val priselY = revolution.y + y
                var scale = 1f + Mathf.log(3f, orbitPos + 3f) * 0.2f - 0.2f
                val perspectiveProgress = (revolution.r / maxOutsideRange).coerceIn(0f, 1f)
                val perspective = Interp.smooth(perspectiveProgress)
                scale *= perspective
                Draw.z(Layer.blockOver)
                img.AsShadow(
                    priselX - elevation * 7f * scale,
                    priselY - elevation * 7f * scale,
                    scale,
                    rotation.a.degree.draw
                )
                if (isInPayload)
                    Draw.z(Layer.blockOver + 1f)
                else
                    Draw.z(Layer.bullet - 1f)
                DebugOnly {
                    G.drawDashCircleBreath(priselX, priselY, prismRadius, circleColor)
                }
                img.DrawSize(
                    priselX,
                    priselY,
                    scale,
                    rotation.a.degree.draw
                )
            }
            Draw.reset()
        }

        override fun drawSelect() {
            if (!this.isSelectedByMouse()) return
            Draw.z(Layer.blockOver)
            val pre = expendingSelectCircleTime / cm.inOrbitAmount
            cm.render {
                val curNeed = (orbitPos + 1) * pre
                var progress = ((Inspector.selectingTime - curNeed) / curNeed).coerceIn(0f, 1f)
                if(isRemoved) progress = 1f - progress
                if (progress < 0.01f) return@render
                G.drawDashCircleBreath(
                    this@PrismBuild,
                    (Agl + (prismRadius * 2 * orbitPos)) * progress,
                    circleColor, alpha = 0.7f
                )
            }
        }

        override fun unit(): MdtUnit {
            unit.tile(this)
            unit.team(team)
            return (unit as MdtUnit)
        }

        override fun control(type: LAccess, p1: Double, p2: Double, p3: Double, p4: Double) {
            if (type == LAccess.shoot && !unit.isPlayer && !p3.isZero) {
                logicAngle = this.angleTo((p1 * tilesize).toFloat(), (p2 * tilesize).toFloat())
                logicControlTime = 60f
            }
            super.control(type, p1, p2, p3, p4)
        }

        override fun control(type: LAccess, p1: Any?, p2: Double, p3: Double, p4: Double) {
            if (type == LAccess.shootp && !unit.isPlayer && !p2.isZero) {
                if (p1 is Posc) {
                    logicControlTime = 60f
                    logicAngle = toAngle(x, y, p1.x, p1.y)
                }
            }
            super.control(type, p1, p2, p3, p4)
        }

        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            cm.read(read)
        }

        override fun write(write: Writes) {
            super.write(write)
            cm.write(write)
        }

        override fun range() = realRange
    }
}