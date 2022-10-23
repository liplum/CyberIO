package net.liplum.blocks.power

import arc.Core
import arc.func.Prov
import arc.graphics.g2d.Draw
import arc.math.Angles
import arc.math.Mathf
import arc.struct.ObjectSet
import arc.util.Time
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.Vars
import mindustry.core.Renderer
import mindustry.gen.Building
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import mindustry.world.blocks.power.PowerBlock
import mindustry.world.blocks.power.PowerGraph
import mindustry.world.meta.Stat
import mindustry.world.meta.StatUnit
import net.liplum.DebugOnly
import net.liplum.R
import net.liplum.Settings
import net.liplum.Var
import net.liplum.common.entity.Radiation
import net.liplum.common.math.PolarX
import plumy.dsl.DrawLayer
import net.liplum.input.smoothPlacing
import net.liplum.input.smoothSelect
import plumy.core.ClientOnly
import plumy.core.WhenNotPaused
import net.liplum.utils.WhenTheSameTeam
import plumy.animation.ContextDraw.Draw
import net.liplum.consumer.powerStore
import net.liplum.utils.sub
import net.liplum.registry.CioStats
import net.liplum.render.AsShadow
import net.liplum.render.G
import net.liplum.render.drawEffectCirclePlace
import net.liplum.utils.addPowerUseStats
import plumy.core.Serialized
import plumy.core.assets.EmptyTR
import plumy.core.math.*
import kotlin.math.min

private typealias PowerUse = Float

open class WirelessTower(name: String) : PowerBlock(name) {
    @JvmField var range = 300f
    @JvmField var distributeSpeed = 5f
    @ClientOnly @JvmField var radiationSpeed = 0.01f
    @JvmField var reactivePower = 0.1f
    @JvmField var dstExtraPowerConsumeFactor = 1f
    @JvmField var dst2CostRate: WirelessTowerBuild.(Distance) -> PowerUse = { dst ->
        1f + dst / realRange * dstExtraPowerConsumeFactor
    }
    @ClientOnly @JvmField var BaseTR = EmptyTR
    @ClientOnly @JvmField var CoilTR = EmptyTR
    @ClientOnly @JvmField var CoreTR = EmptyTR
    @ClientOnly @JvmField var SupportTR = EmptyTR
    @ClientOnly @JvmField var rotationRadius = 0.7f
    @ClientOnly @JvmField var maxSelectedCircleTime = Var.SelectedCircleTime
    @JvmField var range2Stroke: (Float) -> Float = { (it / 100f).coerceAtLeast(1f) }
    @ClientOnly @JvmField var radiationAlpha = 0.8f

    init {
        buildType = Prov { WirelessTowerBuild() }
        consumesPower = true
        updateInUnits = true
        alwaysUpdateInUnits = true
    }

    override fun init() {
        consumePowerDynamic<WirelessTowerBuild> {
            it.lastNeed.coerceAtLeast(reactivePower)
        }
        super.init()
        clipSize = range * 1.5f
    }

    override fun load() {
        super.load()
        BaseTR = this.sub("base")
        CoilTR = this.sub("coil")
        CoreTR = this.sub("core")
        SupportTR = this.sub("support")
    }

    override fun setStats() {
        super.setStats()
        stats.remove(Stat.powerUse)
        addPowerUseStats()
        stats.add(CioStats.powerTransferSpeed, distributeSpeed * 60f, StatUnit.powerSecond)
        stats.add(Stat.powerRange, range, StatUnit.blocks)
    }

    override fun icons() = arrayOf(BaseTR, SupportTR, CoilTR)
    override fun drawPlace(x: Int, y: Int, rotation: Int, valid: Boolean) {
        super.drawPlace(x, y, rotation, valid)
        val range = range * smoothPlacing(maxSelectedCircleTime + range * Var.MaxRangeCircleTimeFactor)
        drawEffectCirclePlace(x, y, R.C.Power, range, {
            val consPower = block.consPower
            block.hasPower && consPower != null && consPower.buffered
        }, stroke = range2Stroke(this.range)) {
            G.wrappedSquareBreath(this)
        }
    }

    open inner class WirelessTowerBuild : Building() {
        @Serialized
        @JvmField var lastNeed = 0f
        val realRange: Float
            get() = range
        val realSpeed: Float
            get() = distributeSpeed * edelta()
        @ClientOnly @JvmField
        var radiation = Radiation()
        @ClientOnly
        val realRadiationSpeed: Float
            get() = radiationSpeed * Mathf.log(3f, timeScale + 2f)
        @ClientOnly @JvmField
        var pingingCount = 0
        val powerGraphsTemp = ObjectSet<PowerGraph>()
        override fun updateTile() {
            lastNeed = 0f
            powerGraphsTemp.clear()
            if (power.status.isZero || (power.graph.all.size == 1 && power.graph.all.first() == this)) return
            forEachBufferedInRange {
                val powerCons = it.block.consPower
                val power = it.power
                val request = powerCons.requestedPower(it)
                if (!request.isZero && powerCons.capacity > 0) {
                    val provided = min(request, realSpeed * Time.delta)
                    it.powerStore += provided
                    lastNeed += provided * dst2CostRate(it.dst(this))
                    powerGraphsTemp.add(power.graph)
                }
            }
            for (graph in powerGraphsTemp) {
                graph.update()
            }
        }

        override fun drawSelect() {
            val range = realRange * smoothSelect(maxSelectedCircleTime + realRange * Var.MaxRangeCircleTimeFactor)
            G.dashCircleBreath(
                x, y, range,
                R.C.Power, stroke = range2Stroke(this.realRange)
            )
            forEachBufferedInRange(range) {
                G.wrappedSquareBreath(it)
            }
        }
        @ClientOnly
        val centerRadius: Float
            get() = size * Vars.tilesize * 2f
        @ClientOnly
        val orientation = PolarX(0f, 0f)
        @ClientOnly
        val rotationRadiusSpeed: Float
            get() = rotationRadius / 25f

        override fun draw() {
            val viewX = Core.camera.position.x
            val viewY = Core.camera.position.y
            val targetRad = Angles.angle(
                x, y,
                viewX, viewY
            ).radian
            DebugOnly {
                G.dashCircle(x, y, centerRadius, alpha = 0.2f)
            }
            WhenNotPaused {
                if (Core.camera.position.dst(x, y) > centerRadius) {
                    orientation.a = targetRad
                    orientation.approachR(rotationRadius, rotationRadiusSpeed * Time.delta)
                } else {
                    orientation.approachR(0f, rotationRadiusSpeed * Time.delta)
                }
            }
            val offsetX = orientation.x
            val offsetY = orientation.y
            Draw.z(Layer.blockUnder)
            BaseTR.Draw(x, y)
            SupportTR.AsShadow(x, y, 1.1f)
            Draw.z(Layer.blockUnder + 0.1f)
            SupportTR.Draw(x, y)
            Draw.z(Layer.block + 1f)
            Drawf.shadow(CoilTR, x + offsetX - 0.5f, y + offsetY - 0.5f)
            CoilTR.Draw(x + offsetX, y + offsetY)
            WhenTheSameTeam {
                if (Time.time % Var.WirelessTowerPingFrequency <= 1f) {
                    pingingCount--
                }
                if (
                    Settings.ShowWirelessTowerCircle &&
                    pingingCount < Var.WirelessTowerInitialPingingNumber
                ) {
                    // Render radiations
                    val selfPower = this.power.status
                    if (selfPower.isZero || selfPower.isNaN() ||
                        (power.graph.all.size == 1 && power.graph.all.first() == this)
                    ) return
                    val realRange = realRange
                    val step = realRadiationSpeed * realRange
                    radiation.apply {
                        WhenNotPaused {
                            range += step
                            if (range >= realRange) {
                                range = 0f
                                pingingCount++
                            }
                        }
                        DrawLayer(Layer.power + 1f) {
                            val progress = range / realRange
                            G.circle(
                                x, y,
                                rad = progress.pow2OutIntrp * realRange,
                                color = R.C.Power,
                                alpha = Renderer.laserOpacity * radiationAlpha,
                                stroke = (realRange / 100f).coerceAtLeast(1f) * (1f - progress).pow2OutIntrp,
                            )
                        }
                    }
                } else {
                    radiation.range = 0f
                }
            }
        }

        open fun forEachBufferedInRange(range: Float = realRange, cons: (Building) -> Unit) {
            Vars.indexer.eachBlock(
                this, range,
                {
                    if (it is WirelessTowerBuild) return@eachBlock false
                    val consPower = it.block.consPower
                    val power = it.power
                    if (power == null || consPower == null) return@eachBlock false
                    consPower.buffered && this !in power.graph.consumers
                },
                cons
            )
        }

        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            lastNeed = read.f()
        }

        override fun write(write: Writes) {
            super.write(write)
            write.f(lastNeed)
        }
    }
}