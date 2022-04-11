package net.liplum.blocks.wireless

import arc.graphics.g2d.Draw
import arc.math.Interp
import arc.math.Mathf
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.Vars
import mindustry.core.Renderer
import mindustry.gen.Building
import mindustry.graphics.Layer
import mindustry.graphics.Pal
import mindustry.world.blocks.power.ConditionalConsumePower
import mindustry.world.blocks.power.PowerBlock
import net.liplum.ClientOnly
import net.liplum.WhenNotPaused
import net.liplum.utils.G
import net.liplum.utils.drawXY
import net.liplum.utils.isZero
import java.util.*
import kotlin.math.min

data class Radiation(var range: Float)
open class WirelessTower(name: String) : PowerBlock(name) {
    @JvmField var range = 300f
    @JvmField var distributeSpeed = 5f
    @JvmField var radiationSpeed = 0.01f
    @JvmField var maxRadiation = 1
    @JvmField var reactivePower = 0.1f
    @JvmField var dst2CostRate: WirelessTowerBuild.(Float) -> Float = {
        1f + it * 1.5f / realRange
    }

    init {
        hasPower = true
        consumesPower = true
        update = true
        solid = true
        canOverdrive = true
        consumes.powerDynamic<WirelessTowerBuild> {
            it.lastNeed.coerceAtLeast(reactivePower)
        }
    }

    override fun init() {
        super.init()
        clipSize = range * 1.5f
    }

    override fun drawPlace(x: Int, y: Int, rotation: Int, valid: Boolean) {
        super.drawPlace(x, y, rotation, valid)
        G.drawDashCircle(x.drawXY, y.drawXY, range, Pal.power)
    }

    open inner class WirelessTowerBuild : Building() {
        @JvmField var lastNeed = 0f
        val realRange: Float
            get() = range * Mathf.log(2f, timeScale + 1f)
        val realSpeed: Float
            get() = distributeSpeed * edelta()
        @ClientOnly @JvmField
        var radiations = LinkedList<Radiation>().apply {
            for (i in 0 until maxRadiation) {
                add(Radiation(range * i / maxRadiation))
            }
        }
        @ClientOnly
        val realRadiationSpeed: Float
            get() = radiationSpeed * Mathf.log(3f, timeScale + 2f)

        override fun updateTile() {
            if (this.power.status.isNaN()) return
            lastNeed = 0f
            forEachTargetInRange {
                val powerCons = it.block.consumes.power
                val power = it.power
                val originalStatus = power.status
                var request = powerCons.requestedPower(it)
                if (powerCons.buffered) {
                    if (!request.isZero() && powerCons.capacity > 0) {
                        val provided = min(request, realSpeed)
                        power.status = Mathf.clamp(
                            originalStatus + provided / powerCons.capacity
                        )
                        lastNeed += provided * dst2CostRate(it.dst(this))
                    }
                } else {
                    if (powerCons is ConditionalConsumePower)
                        request = if (request.isZero())
                            powerCons.usage
                        else
                            request
                    if (request.isZero()) return@forEachTargetInRange
                    val rest = (1f - originalStatus) * request
                    val provided = min(rest, realSpeed)
                    power.status = Mathf.clamp(
                        originalStatus + provided / request
                    )
                    lastNeed += provided * dst2CostRate(it.dst(this))
                }
            }
        }

        override fun drawSelect() {
            G.drawDashCircle(x, y, range, Pal.power, storke = realRange / 100f)
        }

        override fun draw() {
            super.draw()
            val realRange = realRange
            val step = realRadiationSpeed * realRange
            val iterator = radiations.iterator()
            while (iterator.hasNext()) {
                val cur = iterator.next()
                WhenNotPaused {
                    cur.range += step
                    cur.range %= realRange
                }
                Draw.z(Layer.power + 1f)
                val progress = cur.range / realRange
                val nonlinearProgress = Interp.pow2Out.apply(progress)
                G.circle(
                    x, y,
                    nonlinearProgress * realRange,
                    Pal.power, Renderer.laserOpacity,
                    realRange / 100f
                )
            }
        }

        open fun forEachTargetInRange(cons: (Building) -> Unit) {
            Vars.indexer.eachBlock(
                this, range,
                { it.block.hasPower && it.block.consumes.hasPower() && it != this },
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