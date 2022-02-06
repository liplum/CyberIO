package net.liplum.blocks.underdrive

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Lines
import arc.graphics.g2d.TextureRegion
import arc.math.Mathf
import arc.math.geom.Geometry
import arc.scene.ui.Slider
import arc.scene.ui.layout.Table
import arc.util.Time
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.Vars
import mindustry.gen.Building
import mindustry.graphics.Drawf
import mindustry.graphics.Pal
import mindustry.logic.Ranged
import mindustry.ui.Bar
import mindustry.world.blocks.power.PowerGenerator
import mindustry.world.meta.BlockGroup
import mindustry.world.meta.Stat
import mindustry.world.meta.StatUnit
import net.liplum.R
import net.liplum.ui.bars.ReverseBar
import net.liplum.utils.G
import net.liplum.utils.WorldUtil
import net.liplum.utils.bundle
import net.liplum.utils.subA
import kotlin.math.max

const val MagicNSpiralRate = 0.1125f
const val MagicNSpiralMin = 0.025f

open class UnderdriveProjector(name: String?) : PowerGenerator(name) {
    var reload = 60f
    var range = 40f
    /**
     * The less value the slower speed.[0,1]
     */
    var maxSlowDownRate = 0.2f
    var spiralRotateSpeed = 2f
    var color: Color = R.C.LightBlue
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

    override fun drawPlace(x: Int, y: Int, rotation: Int, valid: Boolean) {
        super.drawPlace(x, y, rotation, valid)
        G.drawDashCircle(this, x, y, range, color)
        Vars.indexer.eachBlock(
            Vars.player.team(),
            WorldUtil.toDrawXY(this, x),
            WorldUtil.toDrawXY(this, y),
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
            R.Bar.SlowDownName
        ) {
            ReverseBar(
                { R.Bar.SlowDown.bundle((it.realSlowDown * 100).toInt()) },
                { color },
                { it.restEfficiency / 1f }
            )
        }
        bars.add<UnderdriveBuild>(
            R.Bar.EfficiencyAbsorptionName
        ) {
            Bar(
                { R.Bar.EfficiencyAbsorption.bundle((it.productionEfficiency * 100).toInt()) },
                { Pal.powerBar },
                { it.productionEfficiency / 1f }
            )
        }
    }

    open inner class UnderdriveBuild : GeneratorBuild(), Ranged {
        var charge = Mathf.random(reload)
        var curGear = 1
            set(value) {
                field = value.coerceAtLeast(1)
            }
        var underdrivedBlocks: Int = 0
        override fun range() = realRange
        open val restEfficiency: Float
            get() = 1f - realSlowDown
        open val realRange: Float
            get() {
                /*val eff = efficiency()
                val factor = Mathf.lerp(-eff + 0.5f, -eff * eff, 0.5f)
                return range * factor*/
                return range
            }
        open val realSlowDown: Float
            get() {
                if (maxGear == 1) {
                    return maxSlowDownRate
                }
                return maxSlowDownRate * (curGear.toFloat() / maxGear)
            }
        open val realSpiralRotateSpeed: Float
            get() {
                val percent = underdrivedBlocks / maxPowerEFFUnBlocksReq.toFloat()
                val factor = Mathf.lerp(2f * percent + 0.5f, percent * percent, 0.5f)
                return spiralRotateSpeed * factor
            }

        open fun forEachTargetInRange(cons: (Building) -> Unit) {
            Vars.indexer.eachBlock(
                this, realRange,
                { it.block.canOverdrive },
                cons
            )
        }

        override fun remove() {
            super.remove()
            forEachTargetInRange {
                it.resetBoost()
            }
        }

        override fun updateTile() {
            charge += Time.delta
            if (charge >= reload) {
                var underdrivedBlock = 0
                charge = 0f
                forEachTargetInRange {
                    underdrivedBlock++
                    it.applyBoostOrSlow(restEfficiency, reload + 1f)
                }
                this.underdrivedBlocks = underdrivedBlock
                val absorption = (underdrivedBlock / maxPowerEFFUnBlocksReq.toFloat())
                    .coerceAtMost(2f)
                val magnification = realSlowDown * 0.3f
                productionEfficiency = absorption + magnification
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
            // Draw spiral
            if (underdrivedBlocks > 0) {
                G.init()
                Draw.color(Color.black)
                // Fade in&out
                Draw.alpha(Mathf.absin(Time.time, 10f, 1f) / 2)
                val scale = Mathf.lerp(1f, G.sin, 0.5f)
                val sr = scale * MagicNSpiralRate * realRange
                val srm = 1f * realRange * MagicNSpiralMin
                Draw.rect(
                    spiralTR, x, y,
                    G.Dx(spiralTR) * sr + srm,
                    G.Dy(spiralTR) * sr + srm,
                    Time.time * realSpiralRotateSpeed
                )
            }
            // Draw waves
            val f = 1f - Time.time / 100f % 1f
            Draw.color(color)
            Draw.alpha(1f)
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
        }

        override fun write(write: Writes) {
            super.write(write)
            write.s(curGear)
        }

        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            curGear = read.s().toInt()
        }
    }
}
