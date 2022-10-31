package net.liplum.blocks.prism

import arc.func.Prov
import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.math.Angles
import arc.math.Mathf
import arc.math.geom.Geometry
import arc.struct.EnumSet
import arc.struct.Seq
import arc.util.Time
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.Vars.tilesize
import mindustry.content.UnitTypes
import mindustry.entities.TargetPriority
import mindustry.entities.bullet.BulletType
import mindustry.gen.*
import mindustry.graphics.Layer
import mindustry.graphics.Pal
import mindustry.logic.LAccess
import mindustry.logic.Ranged
import mindustry.world.Block
import mindustry.world.Tile
import mindustry.world.blocks.ControlBlock
import mindustry.world.blocks.defense.turrets.ContinuousTurret
import mindustry.world.blocks.defense.turrets.LaserTurret
import mindustry.world.blocks.defense.turrets.Turret
import mindustry.world.meta.BlockFlag
import mindustry.world.meta.BlockGroup
import net.liplum.DebugOnly
import net.liplum.R
import net.liplum.api.cyber.BOTTOM
import net.liplum.api.cyber.RIGHT
import net.liplum.api.prism.PrismBlackList.canDisperse
import net.liplum.api.prism.PrismDataColor
import net.liplum.api.prism.PrismRegistry.isDuplicate
import net.liplum.api.prism.PrismRegistry.setDuplicate
import net.liplum.blocks.prism.CrystalManager.Companion.read
import net.liplum.blocks.prism.CrystalManager.Companion.write
import net.liplum.common.math.PolarX
import net.liplum.common.util.percentI
import net.liplum.input.Inspector
import net.liplum.input.Inspector.isSelected
import net.liplum.input.smoothSelect
import net.liplum.math.quadratic
import net.liplum.registry.CioStats
import net.liplum.render.AsShadow
import net.liplum.render.G
import net.liplum.utils.*
import plumy.animation.AnimatedColor
import plumy.animation.ContextDraw.Draw
import plumy.animation.ContextDraw.DrawScale
import plumy.core.ClientOnly
import plumy.core.Serialized
import plumy.core.assets.EmptySounds
import plumy.core.assets.EmptyTR
import plumy.core.assets.EmptyTRs
import plumy.core.math.*
import plumy.dsl.AddBar
import plumy.dsl.DrawLayer
import plumy.dsl.bundle
import plumy.dsl.copy
import kotlin.math.abs
import kotlin.math.log2

open class Prism(name: String) : Block(name) {
    var PS: FUNC = quadratic(1.2f, 0.2f)
    /** Above ground level.*/
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
    @ClientOnly @JvmField var BaseTR = EmptyTR
    @ClientOnly @JvmField var CrystalTRs = EmptyTRs
    @ClientOnly @JvmField var UpTR = EmptyTR
    @ClientOnly @JvmField var DownTR = EmptyTR
    @ClientOnly @JvmField var LeftTR = EmptyTR
    @ClientOnly @JvmField var RightTR = EmptyTR
    @ClientOnly @JvmField var RightUpStartTR = EmptyTR
    @ClientOnly @JvmField var RightUpEndTR = EmptyTR
    @ClientOnly @JvmField var LeftDownStartTR = EmptyTR
    @ClientOnly @JvmField var LeftDownEndTR = EmptyTR
    @ClientOnly @JvmField var Right2BottomTRs = EmptyTRs
    @ClientOnly @JvmField var maxSelectedCircleTime = 30f
    @JvmField var crystalSounds = EmptySounds
    @JvmField var crystalSoundVolume = 1f
    @JvmField var crystalSoundPitchRange = 0.5f..1.5f

    init {
        buildType = Prov { PrismBuild() }
        updateInUnits = true
        alwaysUpdateInUnits = true
        absorbLasers = true
        update = true
        solid = true
        outlineIcon = true
        priority = TargetPriority.base
        group = BlockGroup.turrets
        flags = EnumSet.of(BlockFlag.turret)
        noUpdateDisabled = true
        sync = true
    }

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
        Right2BottomTRs = arrayOf(RightTR, UpTR, LeftTR, DownTR)
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

    override fun setStats() {
        super.setStats()
        stats.add(CioStats.maxObelisk, "${maxCrystal - 1}")
    }

    override fun minimapColor(tile: Tile) = animatedColor.color.rgba8888()

    companion object {
        @JvmField @ClientOnly
        val animatedColor = AnimatedColor(
            R.C.PrismRgbFG, useGlobalTime = true
        )
    }

    override fun setBars() {
        super.setBars()
        AddBar<PrismBuild>(R.Bar.PrismN,
            {
                "${R.Bar.Prism.bundle}: $crystalAmount"
            }, { animatedColor.color },
            { crystalAmount.toFloat() / maxCrystal }
        )
        DebugOnly {
            AddBar<PrismBuild>("progress",
                { "${"bar.loadprogress".bundle}: ${cm.progress.percentI}" },
                { Pal.power },
                { cm.progress }
            )
            AddBar<PrismBuild>("prism-status",
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
                    img = CrystalTRs.random()
                }
            }
            addCrystalCallback = {
                revolution = PolarX(0f, 0f)
                rotation = PolarX(prismRadius, 0f)
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
        var logicControlTime = -1f
        val controlByLogic: Boolean
            get() = logicControlTime > 0f
        var logicAngle = 0f
        val realRange: Float
            get() = Agl + (prismRadius * 2 * crystalAmount)
        open val Crystal.circleColor: Color
            get() = R.C.PrismRgbFG[(orbitPos + id) % 3]

        override fun onRemoved() =
            cm.clearObelisk()

        override fun dropped() {
            checkObelisk()
        }

        override fun onProximityUpdate() {
            super.onProximityUpdate()
            checkObelisk()
        }

        open fun checkObelisk() {
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
            var soundPlayed = false
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
                        it.dst(priselX, priselY) <= prismRadius
                    ) {
                        if (!it.data.isDuplicate) {
                            it.passThrough()
                        }
                        if (!soundPlayed && !isActivated) {
                            crystalSounds.random().at(x, y, crystalSoundPitchRange.random(), crystalSoundVolume)
                            soundPlayed = true
                        }
                        lastPassThroughTime = 0f
                    }
                }
            }
        }
        /**
         * The Current is considered as green.
         */
        fun Bullet.passThrough() {
            if (!type.canDisperse) return
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
            if (tintBullet && !this.type.isIgnoreTinted) {
                tintBullet(copyRed, this, copyBlue, this.type)
            }
            setDuplicate(copyRed, this, copyBlue, this.data)
        }

        fun tintBullet(r: Bullet, g: Bullet, b: Bullet, type: BulletType) {
            val rgbs = tintedRGB(type)
            r.type = rgbs[0]
            g.type = rgbs[1]
            b.type = rgbs[2]
        }

        fun setDuplicate(r: Bullet, g: Bullet, b: Bullet, data: Any?) {
            if (data is Color) {
                r.data = PrismDataColor.RgbFG[0]
                g.data = PrismDataColor.RgbFG[1]
                b.data = PrismDataColor.RgbFG[2]
            } else {
                r.setDuplicate()
                g.setDuplicate()
                b.setDuplicate()
            }
        }

        open fun Bullet.handleDuplicate(
            angleOffset: Float = 0f,
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
            val progress = cm.progress.smooth
            Draw.alpha(1f - progress)
            Draw.rect(RightUpStartTR, x, y)
            Draw.rect(RightUpStartTR, x, y, 90f)
            Draw.rect(LeftDownStartTR, x, y)
            Draw.rect(LeftDownStartTR, x, y, -90f)
            Draw.alpha(progress)
            Draw.rect(RightUpEndTR, x, y)
            Draw.rect(RightUpEndTR, x, y, 90f)
            Draw.rect(LeftDownEndTR, x, y)
            Draw.rect(LeftDownEndTR, x, y, -90f)
            Draw.color()
            val delta = progress * sizeOpen * G.sclx

            Draw.z(Layer.blockOver)

            for (side in RIGHT..BOTTOM) {
                val dir = Geometry.d4[side]
                Right2BottomTRs[side].Draw(x + delta * dir.x, y + delta * dir.y)
                Right2BottomTRs[side].AsShadow(x + delta * dir.x, y + delta * dir.y)
            }
            val isSelected = this.isSelected()
            // draw Select
            if (isSelected) {
                Draw.z(Layer.blockOver)
                val pre = expendingSelectCircleTime / cm.inOrbitAmount
                cm.render {
                    val curNeed = (orbitPos + 1) * pre
                    var selectProgress = ((Inspector.selectingTime + pre - curNeed) / curNeed).smooth
                    if (isRemoved) selectProgress = 1f - selectProgress
                    if (selectProgress < 0.01f) return@render
                    G.dashCircleBreath(
                        this@PrismBuild,
                        (Agl + (prismRadius * 2 * orbitPos)) * selectProgress,
                        circleColor, alpha = 0.7f
                    )
                }
            }
            // draw Crystal
            cm.render {
                lastPassThroughTime += Time.delta
                val priselX = revolution.x + x
                val priselY = revolution.y + y
                var scale = 1f + Mathf.log(3f, orbitPos + 3f) * 0.2f - 0.2f
                val perspectiveProgress = (revolution.r / maxOutsideRange).smooth
                scale *= perspectiveProgress
                Draw.z(Layer.blockOver)
                img.AsShadow(
                    priselX - elevation * 7f * scale,
                    priselY - elevation * 7f * scale,
                    scale,
                    rotation.a.degree.draw
                )
                if (isInPayload) Draw.z(Layer.blockOver + 1f)
                else Draw.z(Layer.bullet - 1f)
                if (isSelected)
                    G.dashCircleBreath(priselX, priselY, prismRadius * smoothSelect(maxSelectedCircleTime), circleColor)
                DrawLayer(if (isActivated) Layer.bullet else Draw.z()) {
                    img.DrawScale(
                        priselX,
                        priselY,
                        scale,
                        rotation.a.degree.draw
                    )
                }
            }
            Draw.reset()
        }

        override fun drawSelect() {
            // See draw()
        }

        override fun unit(): MUnit {
            unit.tile(this)
            unit.team(team)
            return (unit as MUnit)
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