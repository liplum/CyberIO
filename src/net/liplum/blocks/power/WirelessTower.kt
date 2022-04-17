package net.liplum.blocks.power

import arc.Core
import arc.graphics.g2d.Draw
import arc.math.Angles
import arc.math.Interp
import arc.math.Mathf
import arc.math.geom.Vec2
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.Vars
import mindustry.core.Renderer
import mindustry.gen.Building
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import mindustry.world.blocks.power.ConditionalConsumePower
import mindustry.world.blocks.power.PowerBlock
import net.liplum.ClientOnly
import net.liplum.R
import net.liplum.WhenNotPaused
import net.liplum.lib.entity.Radiations
import net.liplum.lib.animations.anis.Draw
import net.liplum.utils.*
import kotlin.math.min

open class WirelessTower(name: String) : PowerBlock(name) {
    @JvmField var range = 300f
    @JvmField var distributeSpeed = 5f
    @JvmField var radiationSpeed = 0.01f
    @JvmField var maxRadiation = 1
    @JvmField var reactivePower = 0.1f
    @JvmField var dst2CostRate: WirelessTowerBuild.(Float) -> Float = {
        1f + it * 1.5f / realRange
    }
    lateinit var BaseTR: TR
    lateinit var CoilTR: TR
    lateinit var CoreTR: TR
    lateinit var SupportTR: TR
    @ClientOnly @JvmField var rotationRadius = 0.7f

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
        clipSize = range * 1.5f
        super.init()
    }

    override fun load() {
        super.load()
        BaseTR = this.sub("base")
        CoilTR = this.sub("coil")
        CoreTR = this.sub("core")
        SupportTR = this.sub("support")
    }

    override fun icons() = arrayOf(BaseTR, SupportTR, CoilTR)
    override fun drawPlace(x: Int, y: Int, rotation: Int, valid: Boolean) {
        super.drawPlace(x, y, rotation, valid)
        G.drawDashCircle(x.drawXY, y.drawXY, range, R.C.Power)
        Vars.indexer.eachBlock(
            Vars.player.team(),
            x.toDrawXY(this),
            y.toDrawXY(this),
            range,
            {
                it.block.hasPower && it.block.consumes.hasPower()
            }) {
            G.drawSelected(it, R.C.Power)
        }
    }

    open inner class WirelessTowerBuild : Building() {
        @JvmField var lastNeed = 0f
        val realRange: Float
            get() = range * Mathf.log(2f, timeScale + 1f)
        val realSpeed: Float
            get() = distributeSpeed * edelta()
        @ClientOnly @JvmField
        var radiations = Radiations(maxRadiation) { i, r ->
            r.range = range * i / maxRadiation
        }
        @ClientOnly
        val viewVec = Vec2(rotationRadius, 0f)
        @ClientOnly
        val realRadiationSpeed: Float
            get() = radiationSpeed * Mathf.log(3f, timeScale + 2f)

        override fun updateTile() {
            if (power.status <= 0.999f) return
            lastNeed = 0f
            forEachTargetInRange {
                val powerCons = it.block.consumes.power
                val power = it.power
                val originalStatus = power.status
                var request = powerCons.requestedPower(it)
                if (powerCons.buffered) {
                    if (!request.isZero && powerCons.capacity > 0) {
                        val provided = min(request, realSpeed)
                        power.status = Mathf.clamp(
                            originalStatus + provided / powerCons.capacity
                        )
                        lastNeed += provided * dst2CostRate(it.dst(this))
                    }
                } else {
                    if (powerCons is ConditionalConsumePower)
                        request = if (request.isZero)
                            powerCons.usage
                        else
                            request
                    if (request.isZero) return@forEachTargetInRange
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
            G.drawDashCircle(x, y, range, R.C.Power, storke = (realRange / 100f).coerceAtLeast(1f))
            forEachTargetInRange {
                G.drawSelected(it, R.C.Power)
            }
        }

        override fun draw() {
            val viewX = Core.camera.position.x
            val viewY = Core.camera.position.y
            val newDegree = Angles.angle(
                x, y,
                viewX, viewY
            )
            viewVec.setAngle(
                Angles.moveToward(
                    viewVec.angle(),
                    newDegree,
                    3f
                )
            )
            val offsetX = viewVec.x
            val offsetY = viewVec.y
            Draw.z(Layer.blockUnder)
            BaseTR.Draw(x, y)
            Drawf.shadow(SupportTR, x - 1f, y - 1f)
            Draw.z(Layer.blockUnder + 0.1f)
            SupportTR.Draw(x, y)
            Draw.z(Layer.block + 1f)
            Drawf.shadow(CoilTR, x + offsetX - 0.5f, y + offsetY - 0.5f)
            CoilTR.Draw(x + offsetX, y + offsetY)
            //Radiation
            val selfPower = this.power.status
            if (selfPower.isZero || selfPower.isNaN()) return
            val realRange = realRange
            val step = realRadiationSpeed * realRange
            radiations.forEach {
                WhenNotPaused {
                    it.range += step
                    it.range %= realRange
                }
                Draw.z(Layer.power + 1f)
                val progress = it.range / realRange
                val nonlinearProgress = Interp.pow2Out.apply(progress)
                G.circle(
                    x, y,
                    nonlinearProgress * realRange,
                    R.C.Power, Renderer.laserOpacity,
                    (realRange / 100f).coerceAtLeast(1f)
                )
            }
        }

        open fun forEachTargetInRange(cons: (Building) -> Unit) {
            Vars.indexer.eachBlock(
                this, range,
                { it.block.hasPower && it.block.consumes.hasPower() && it !is WirelessTowerBuild },
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