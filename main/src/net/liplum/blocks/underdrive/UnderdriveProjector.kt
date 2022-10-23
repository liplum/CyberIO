package net.liplum.blocks.underdrive

import arc.func.Prov
import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Lines
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
import mindustry.gen.Bullet
import mindustry.gen.Groups
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import mindustry.graphics.Pal
import mindustry.input.Placement
import mindustry.logic.Ranged
import mindustry.world.Tile
import mindustry.world.blocks.power.PowerGenerator
import mindustry.world.meta.BlockGroup
import mindustry.world.meta.Stat
import mindustry.world.meta.StatUnit
import net.liplum.DebugOnly
import net.liplum.R
import net.liplum.Var
import net.liplum.common.util.format
import net.liplum.common.util.percentI
import net.liplum.input.smoothPlacing
import net.liplum.input.smoothSelect
import net.liplum.render.G
import net.liplum.render.G.realHeight
import net.liplum.render.G.realWidth
import net.liplum.render.drawEffectCirclePlace
import net.liplum.ui.bars.ReverseBar
import net.liplum.utils.addRangeInfo
import net.liplum.utils.sub
import net.liplum.utils.subBundle
import plumy.core.ClientOnly
import plumy.core.Serialized
import plumy.core.assets.EmptyTR
import plumy.dsl.AddBar
import plumy.dsl.NewEffect
import plumy.dsl.bundle
import kotlin.math.max

const val MagicNSpiralRate = 0.1125f
const val MagicNSpiralMin = 0.025f
const val MinSpiralSpeed = 3.5f
const val MaxSpiralSpeed = 16f
const val MagicAlpha = 0.8f

enum class AttenuationType {
    None, Exponential, Additive
}
private typealias UnitC = mindustry.gen.Unit

val SpiralShrink: Effect = NewEffect(30f) {
    val upb = data as UnderdriveProjector.UnderdriveBuild
    val up = upb.block as UnderdriveProjector
    Draw.color(color, fout())
    val scale = Mathf.lerp(1f, G.sin, 0.5f)
    val realRange = upb.realRange
    val sr = scale * MagicNSpiralRate * realRange * fout()
    val srm = realRange * MagicNSpiralMin
    val s = up.spiralTR
    Draw.rect(
        s, x, y,
        s.realWidth * sr + srm,
        s.realHeight * sr + srm,
        Time.time * upb.realSpiralRotateSpeed
    )
    Draw.z(Layer.weather)
    Drawf.shadow(x, y, realRange, 1f * fout())
    Draw.z()
}.layer(Layer.shields)

fun UnderdriveProjector.UnderdriveBuild.spiralShrinking() {
    SpiralShrink.at(x, y, realRange, (block as UnderdriveProjector).color, this)
}

open class UnderdriveProjector(name: String) : PowerGenerator(name) {
    @JvmField var reload = 60f
    @JvmField var range = 40f
    /**
     * The less value the slower speed.[0,1]
     */
    @JvmField var maxSlowDownRate = 0.2f
    @JvmField var spiralRotateSpeed = 2f
    @JvmField var similarAttenuation = AttenuationType.Exponential
    @JvmField var attenuationRateStep = 0.5f
    @JvmField var color: Color = R.C.LightBlue
    @JvmField var slowDownRateEFFReward = 0.3f
    @JvmField var maxPowerEFFBlocksReq = 10
    @JvmField var maxGear = 1
    @ClientOnly @JvmField var spiralTR = EmptyTR
    @ClientOnly @JvmField var maxSelectedCircleTime = Var.SelectedCircleTime

    init {
        buildType = Prov { UnderdriveBuild() }
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

    override fun minimapColor(tile: Tile) = R.C.Shadow.rgba8888()
    override fun init() {
        super.init()
        clipSize = range * 2f
        maxGear = maxGear.coerceAtLeast(1)
        maxPowerEFFBlocksReq = maxPowerEFFBlocksReq.coerceAtLeast(1)
    }

    override fun load() {
        super.load()
        spiralTR = this.sub("spiral")
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
        val range = range * smoothPlacing(maxSelectedCircleTime)
        drawEffectCirclePlace(x, y, color, range, { block.canOverdrive }) {
            G.selectedBreath(this, color)
        }
    }

    override fun setStats() {
        super.setStats()
        stats.remove(Stat.basePowerGeneration)
        stats.add(Stat.basePowerGeneration) {
            it.add(
                subBundle(
                    "stats.power-gen",
                    "${powerProduction * Time.toSeconds} ${StatUnit.powerSecond.localized()}", maxPowerEFFBlocksReq
                )
            )
        }
        stats.add(Stat.speedIncrease) {
            val max = maxSlowDownRate
            val min = maxSlowDownRate / maxGear
            it.add(
                R.Bundle.Gen("speed-increase.range").bundle(
                    -min.percentI, -max.percentI
                )
            )
        }
        stats.add(
            Stat.range,
            range / Vars.tilesize,
            StatUnit.blocks
        )
    }

    override fun setBars() {
        super.setBars()
        addBar<UnderdriveBuild>(R.Bar.SlowDownN) {
            ReverseBar(
                { R.Bar.SlowDown.bundle(it.realSlowDown.percentI) },
                { color },
                { it.restEfficiency / 1f }
            )
        }
        AddBar<UnderdriveBuild>(R.Bar.EfficiencyAbsorptionN,
            { R.Bar.EfficiencyAbsorption.bundle(productionEfficiency.percentI) },
            { Pal.powerBar },
            { productionEfficiency / 1f }
        )
        DebugOnly {
            AddBar<UnderdriveBuild>(
                "spiral-rotate-speed",
                { "RotateSPD:${realSpiralRotateSpeed.format(2)}" },
                { Pal.powerBar },
                { realSpiralRotateSpeed / 10f }
            )
            AddBar<UnderdriveBuild>(
                "alpha",
                { "alpha:${alpha.format(2)}" },
                { Color.blue },
                { alpha / 1f }
            )
            addRangeInfo<UnderdriveBuild>(100f)
        }
    }

    open inner class UnderdriveBuild : GeneratorBuild(), Ranged {
        var charge = reload
        @Serialized
        var curGear = 1
            set(value) {
                field = value.coerceAtLeast(1)
            }
        @ClientOnly
        var buildingProgress: Float = 0f
            set(value) {
                field = value.coerceIn(0f, 1f)
            }
        @Serialized
        var underdrivedBlocks: Int = 0
        @Serialized
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
                val percent = underdrivedBlocks / maxPowerEFFBlocksReq.toFloat()
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

        open fun forEachTargetInRange(range: Float = realRange, cons: (Building) -> Unit) {
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

        inline fun forEachBulletInRange(crossinline cons: (Bullet) -> Unit) {
            Groups.bullet.intersect(x - realRange / 2, y - realRange / 2, realRange, realRange) {
                if (it.dst(this) <= realRange) {
                    cons(it)
                }
            }
        }

        inline fun forEachUnitInRange(crossinline cons: (UnitC) -> Unit) {
            Groups.unit.intersect(x - realRange / 2, y - realRange / 2, realRange, realRange) {
                if (it.dst(this) <= realRange) {
                    cons(it)
                }
            }
        }

        override fun onRemoved() {
            super.onRemoved()
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
                        it.applySlowdown(restEfficiency, reload + 1f)
                    } else if (it is UnderdriveBuild && it != this) {
                        similarInRange++
                    }
                }

                this.underdrivedBlocks = underdrivedBlock
                this.similarInRange = similarInRange
                if (underdrivedBlock > 0) {
                    val absorption = (underdrivedBlock / maxPowerEFFBlocksReq.toFloat())
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
                table.add(Slider(
                    0f, maxGear - 1f, 1f,
                    false
                ).apply {
                    value = curGear.toFloat() - 1
                    moved { configure(Mathf.round(it + 1)) }
                }).width(180f).growX()
            }
        }

        override fun config(): Int = curGear
        override fun drawSelect() {
            val range = realRange * smoothSelect(maxSelectedCircleTime)
            forEachTargetInRange(range) {
                G.selectedBreath(it, color)
            }
            G.dashCircleBreath(x, y, range, color)
        }

        override fun draw() {
            super.draw()
            //Draw shadows
            val realRange = buildingProgress * realRange
            Draw.z(Layer.weather)
            Drawf.shadow(x, y, realRange * (2f + realSlowDown), 1f + realSlowDown)
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
                    spiralTR.realWidth * sr + srm,
                    spiralTR.realHeight * sr + srm,
                    Time.time * realSpiralRotateSpeed
                )
            }

            Draw.reset()
        }

        override fun write(write: Writes) {
            super.write(write)
            write.b(curGear)
            write.s(underdrivedBlocks)
            write.s(similarInRange)
        }

        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            curGear = read.b().toInt()
            underdrivedBlocks = read.s().toInt()
            similarInRange = read.s().toInt()
        }
    }
}