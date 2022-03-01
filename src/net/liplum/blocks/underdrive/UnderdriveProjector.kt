package net.liplum.blocks.underdrive

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Lines
import arc.graphics.g2d.TextureRegion
import arc.math.Mathf
import arc.math.geom.Geometry
import arc.math.geom.Intersector
import arc.math.geom.Point2
import arc.scene.ui.Slider
import arc.scene.ui.layout.Table
import arc.struct.Seq
import arc.util.Nullable
import arc.util.Time
import arc.util.Tmp
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.Vars
import mindustry.entities.Effect
import mindustry.gen.Building
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import mindustry.graphics.Pal
import mindustry.input.Placement
import mindustry.logic.Ranged
import mindustry.ui.Bar
import mindustry.world.Tile
import mindustry.world.blocks.power.PowerGenerator
import mindustry.world.meta.BlockGroup
import mindustry.world.meta.Stat
import mindustry.world.meta.StatUnit
import net.liplum.ClientOnly
import net.liplum.DebugOnly
import net.liplum.R
import net.liplum.ui.bars.ReverseBar
import net.liplum.utils.*
import kotlin.math.max

const val MagicNSpiralRate = 0.1125f
const val MagicNSpiralMin = 0.025f
const val MinSpiralSpeed = 3.5f
const val MaxSpiralSpeed = 16f
const val MagicAlpha = 0.8f

enum class AttenuationType {
    None, Exponential, Additive
}

val SpiralShrink: Effect = Effect(20f) {
    val upb = it.data as UnderdriveProjector.UnderdriveBuild
    val up = upb.block as UnderdriveProjector
    Draw.color(it.color, it.fout())
    val scale = Mathf.lerp(1f, G.sin, 0.5f)
    val realRange = upb.realRange
    val sr = scale * MagicNSpiralRate * realRange * it.fout()
    val srm = realRange * MagicNSpiralMin
    val s = up.spiralTR
    Draw.rect(
        s, it.x, it.y,
        G.Dw(s) * sr + srm,
        G.Dh(s) * sr + srm,
        Time.time * upb.realSpiralRotateSpeed
    )
}.layer(Layer.shields)

fun UnderdriveProjector.UnderdriveBuild.spiralShrinking() {
    SpiralShrink.at(x, y, realRange, (block as UnderdriveProjector).color, this)
}

open class UnderdriveProjector(name: String) : PowerGenerator(name) {
    var reload = 60f
    var range = 40f
    /**
     * The less value the slower speed.[0,1]
     */
    var maxSlowDownRate = 0.2f
    var spiralRotateSpeed = 2f
    var similarAttenuation = AttenuationType.Exponential
    var attenuationRateStep = 0.5f
    var color: Color = R.C.LightBlue
    var slowDownRateEFFReward = 0.3f
    var maxPowerEFFUnBlocksReq = 10
        set(value) {
            field = value.coerceAtLeast(1)
        }
    var maxGear = 1
        set(value) {
            field = value.coerceAtLeast(1)
        }
    lateinit var spiralTR: TextureRegion

    init {
        solid = true
        update = true
        group = BlockGroup.projectors
        hasItems = false
        hasPower = true
        canOverdrive = false
        configurable = true
        saveConfig = true

        config(
            Integer::class.java
        ) { b: UnderdriveBuild, i ->
            b.curGear = i.toInt()
        }

        configClear<UnderdriveBuild> {
            it.curGear = 1
        }
    }

    override fun load() {
        super.load()
        spiralTR = this.subA("spiral")
    }

    override fun changePlacementPath(points: Seq<Point2>, rotation: Int) {
        Placement.calculateNodes(
            points, this, rotation
        ) { point: Point2, other: Point2 ->
            overlaps(
                Vars.world.tile(point.x, point.y),
                Vars.world.tile(other.x, other.y)
            )
        }
    }

    open fun overlaps(@Nullable src: Tile?, @Nullable other: Tile?): Boolean {
        return if (src == null || other == null) true else Intersector.overlaps(
            Tmp.cr1.set(
                src.worldx() + offset,
                src.worldy() + offset,
                range * 2
            ),
            Tmp.r1.setSize((size * Vars.tilesize).toFloat()).setCenter(other.worldx() + offset, other.worldy() + offset)
        )
    }

    override fun drawPlace(x: Int, y: Int, rotation: Int, valid: Boolean) {
        super.drawPlace(x, y, rotation, valid)
        G.drawDashCircle(this, x, y, range, color)
        Vars.indexer.eachBlock(
            Vars.player.team(),
            WorldU.toDrawXY(this, x),
            WorldU.toDrawXY(this, y),
            range,
            {
                it.block.canOverdrive
            }
        ) {
            G.drawSelected(it, color)
        }
    }

    override fun setStats() {
        super.setStats()
        stats.add(
            Stat.speedIncrease,
            -100f * maxSlowDownRate,
            StatUnit.percent
        )
        stats.add(
            Stat.range,
            range / Vars.tilesize,
            StatUnit.blocks
        )
    }

    override fun setBars() {
        super.setBars()
        bars.add<UnderdriveBuild>(
            R.Bar.SlowDownN
        ) {
            ReverseBar(
                { R.Bar.SlowDown.bundle((it.realSlowDown * 100).toInt()) },
                { color },
                { it.restEfficiency / 1f }
            )
        }
        bars.add<UnderdriveBuild>(
            R.Bar.EfficiencyAbsorptionN
        ) {
            Bar(
                { R.Bar.EfficiencyAbsorption.bundle((it.productionEfficiency * 100).toInt()) },
                { Pal.powerBar },
                { it.productionEfficiency / 1f }
            )
        }
        DebugOnly {
            bars.add<UnderdriveBuild>(
                R.Bar.SpiralRotationSpeedN
            ) {
                Bar(
                    { R.Bar.SpiralRotationSpeed.bundle(it.realSpiralRotateSpeed.format(2)) },
                    { Pal.powerBar },
                    { it.realSpiralRotateSpeed / 10f }
                )
            }
            bars.add<UnderdriveBuild>(
                R.Bar.AlphaN
            ) {
                Bar(
                    { R.Bar.Alpha.bundle(it.alpha.format(2)) },
                    { Color.blue },
                    { it.alpha / 1f }
                )
            }
            bars.addRangeInfo<UnderdriveBuild>(100f)
        }
    }

    open inner class UnderdriveBuild : GeneratorBuild(), Ranged {
        var charge = reload
        var curGear = 1
            set(value) {
                field = value.coerceAtLeast(1)
            }
        @ClientOnly
        var buildingProgress: Float = 0f
            set(value) {
                field = value.coerceIn(0f, 1f)
            }
        var underdrivedBlocks: Int = 0
        var similarInRange: Int = 0
        override fun range() = realRange
        open val alpha: Float
            get() = MagicAlpha
        open val restEfficiency: Float
            get() = 1f - realSlowDown
        open val realRange: Float
            get() = range
        open val realSlowDown: Float
            get() {
                if (maxGear == 1) {
                    return maxSlowDownRate
                }
                return maxSlowDownRate * (curGear.toFloat() / maxGear)
            }
        @ClientOnly
        open val canShowSpiral: Boolean
            get() = buildingProgress > 0f || underdrivedBlocks != 0
        @ClientOnly
        open var lastRealSpiralRotateSpeed: Float = 0f
        @ClientOnly
        open val realSpiralRotateSpeed: Float
            get() {
                val percent = underdrivedBlocks / maxPowerEFFUnBlocksReq.toFloat()
                val factor = Mathf.lerp(2f * percent + 0.5f, percent * percent, 0.5f)
                val final = if (canShowSpiral)
                    (spiralRotateSpeed * factor * similarAttenuationFactor * (1f + realSlowDown / 2f))
                        .coerceIn(MinSpiralSpeed, MaxSpiralSpeed)
                else 0f
                if (underdrivedBlocks != 0) {
                    lastRealSpiralRotateSpeed = final
                }
                return final
            }
        open val similarAttenuationFactor: Float
            get() = when (similarAttenuation) {
                AttenuationType.None ->
                    1f
                AttenuationType.Exponential ->
                    Mathf.pow(attenuationRateStep, similarInRange.toFloat())
                AttenuationType.Additive ->
                    (1f - similarInRange * attenuationRateStep).coerceAtLeast(0f)
            }

        open fun forEachTargetInRange(cons: (Building) -> Unit) {
            Vars.indexer.eachBlock(
                this, realRange,
                { it.block.canOverdrive },
                cons
            )
        }

        open fun forEachBuildingInRange(cons: (Building) -> Unit) {
            Vars.indexer.eachBlock(
                this, realRange,
                { true },
                cons
            )
        }

        override fun onRemoved() {
            super.onRemoved()
            forEachTargetInRange {
                it.resetBoost()
            }
            ClientOnly {
                if (canShowSpiral) {
                    this.spiralShrinking()
                }
            }
        }

        override fun updateTile() {
            charge += Time.delta
            val per = Time.delta / reload
            if (productionEfficiency > 0f) {
                buildingProgress += per
            } else {
                buildingProgress -= per
            }
            if (charge >= reload) {
                var underdrivedBlock = 0
                var similarInRange = 0
                charge = 0f
                forEachBuildingInRange {
                    if (it.block.canOverdrive) {
                        underdrivedBlock++
                        it.applyBoostOrSlow(restEfficiency, reload + 1f)
                    } else if (it is UnderdriveBuild && it != this) {
                        similarInRange++
                    }
                }
                this.underdrivedBlocks = underdrivedBlock
                this.similarInRange = similarInRange
                if (underdrivedBlock > 0) {
                    val absorption = (underdrivedBlock / maxPowerEFFUnBlocksReq.toFloat())
                        .coerceAtMost(2f)
                    val reward = realSlowDown * slowDownRateEFFReward
                    productionEfficiency = absorption + reward
                    productionEfficiency *= similarAttenuationFactor
                } else {
                    productionEfficiency = 0f
                }
            }
        }

        override fun buildConfiguration(table: Table) {
            if (maxGear > 1) {
                val gearController = Slider(
                    0f, maxGear - 1f, 1f,
                    false
                )
                gearController.value = curGear.toFloat() - 1
                gearController.moved {
                    configure(Mathf.round(it + 1))
                }
                table.add(gearController)
            }
        }

        override fun config(): Int = curGear
        override fun drawSelect() {
            forEachTargetInRange {
                G.drawSelected(it, color)
            }
            Drawf.dashCircle(x, y, realRange, color)
        }

        override fun draw() {
            super.draw()
            G.init()
            //Draw shadows
            val realRange = buildingProgress * realRange
            Draw.z(Layer.blockUnder)
            Drawf.shadow(x, y, realRange * 2f)
            // Draw waves
            Draw.z(Layer.block)
            val f = 1f - Time.time / 100f % 1f
            Draw.color(color)
            Lines.stroke(2f * f + 0.1f)
            val r = max(
                0f,
                Mathf.clamp(2f - f * 2f) * size * Vars.tilesize / 2f - f - 0.2f
            )
            val w = Mathf.clamp(0.5f - f) * size * Vars.tilesize
            Lines.beginLine()
            for (i in 0..3) {
                Lines.linePoint(
                    x + Geometry.d4(i).x * r + Geometry.d4(i).y * w,
                    y + Geometry.d4(i).y * r - Geometry.d4(i).x * w
                )
                if (f < 0.5f) Lines.linePoint(
                    x + Geometry.d4(i).x * r - Geometry.d4(i).y * w,
                    y + Geometry.d4(i).y * r + Geometry.d4(i).x * w
                )
            }
            Lines.endLine(true)
            Draw.reset()
            // Draw spiral
            if (canShowSpiral) {
                val realSpiralRotateSpeed = if (underdrivedBlocks != 0)
                    realSpiralRotateSpeed
                else lastRealSpiralRotateSpeed
                Draw.z(Layer.shields)
                Draw.color(Color.black)
                // (removed) Fade in&out
                Draw.alpha(alpha)
                val scale = Mathf.lerp(1f, G.sin, 0.5f)
                val sr = scale * MagicNSpiralRate * realRange
                val srm = realRange * MagicNSpiralMin
                Draw.rect(
                    spiralTR, x, y,
                    G.Dw(spiralTR) * sr + srm,
                    G.Dh(spiralTR) * sr + srm,
                    Time.time * realSpiralRotateSpeed
                )
            }

            Draw.reset()
        }

        override fun write(write: Writes) {
            super.write(write)
            write.s(curGear)
            write.s(underdrivedBlocks)
            write.s(similarInRange)
        }

        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            curGear = read.s().toInt()
            underdrivedBlocks = read.s().toInt()
            similarInRange = read.s().toInt()
        }
    }
}