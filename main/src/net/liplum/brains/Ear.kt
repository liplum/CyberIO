package net.liplum.brains

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Lines
import arc.math.Interp
import arc.math.geom.Vec2
import arc.util.Time
import arc.util.Tmp
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.Vars
import mindustry.content.UnitTypes
import mindustry.entities.UnitSorts
import mindustry.entities.Units
import mindustry.gen.*
import mindustry.gen.Unit
import mindustry.graphics.Drawf
import mindustry.graphics.Layer
import mindustry.logic.LAccess
import mindustry.world.Block
import mindustry.world.blocks.ControlBlock
import mindustry.world.meta.Stat
import mindustry.world.meta.StatUnit
import net.liplum.ClientOnly
import net.liplum.DebugOnly
import net.liplum.R
import net.liplum.Serialized
import net.liplum.api.brain.*
import net.liplum.lib.Draw
import net.liplum.lib.DrawSize
import net.liplum.utils.*

open class Ear(name: String) : Block(name), IComponentBlock {
    @JvmField var waveSpeed = 2.5f
    @JvmField var waveWidth = 5f
    @JvmField var waveWidthI = 0.3f
    @JvmField var damage = 2.3f
    @JvmField var damageI = 1.5f
    @JvmField var sensitivity = 0.6f
    @JvmField var sensitivityI = -0.3f
    @JvmField var sonicMaxRadius = 40f
    @JvmField var sonicMaxRadiusI = 0.4f
    @JvmField var powerUse = 2f
    @JvmField var powerUseI = 0.8f
    @JvmField var range = 150f
    @JvmField var rangeI = 0.4f
    @JvmField var reloadTime = 60f
    @JvmField var reloadTimeI = 0.5f
    @JvmField var powerConsumeTime = 30f
    @JvmField var maxSonicWaveNum = 3
    @JvmField var bounce = 1f
    @ClientOnly lateinit var BaseTR: TR
    @ClientOnly lateinit var EarTR: TR
    @ClientOnly @JvmField var scaleTime = 30f
    @ClientOnly @JvmField var maxScale = 0.3f
    @ClientOnly @JvmField var sonicWaveColor: Color = R.C.SonicWave
    override val upgrades: MutableMap<UpgradeType, Upgrade> = HashMap()

    init {
        solid = true
        update = true
        hasPower = true
        sync = true
        canOverdrive = false
    }

    override fun init() {
        clipSize = (range * (1f + rangeI)) + (sonicMaxRadius * (1f * rangeI))
        checkInit()
        consumePowerDynamic<EarBuild> {
            if (it.lastRadiateTime < powerConsumeTime) it.realPowerUse else 0f
        }
        super.init()
    }

    override fun load() {
        super.load()
        BaseTR = this.sub("base")
        EarTR = region
    }

    override fun icons() = arrayOf(BaseTR, EarTR)
    override fun setBars() {
        super.setBars()
        DebugOnly {
            addBrainInfo<EarBuild>()
        }
    }

    override fun drawPlace(x: Int, y: Int, rotation: Int, valid: Boolean) {
        super.drawPlace(x, y, rotation, valid)
        Drawf.dashCircle(x * Vars.tilesize + offset, y * Vars.tilesize + offset, range, sonicWaveColor)
    }

    override fun setStats() {
        super.setStats()
        this.addUpgradeComponentStats()
        stats.remove(Stat.powerUse)
        stats.add(Stat.powerUse, powerUse * 60f, StatUnit.powerSecond)
    }

    open inner class EarBuild : Building(), IUpgradeComponent, ControlBlock {
        // <editor-fold desc="Heimdall">
        override val scale: SpeedScale = SpeedScale()
        override var directionInfo: Direction2 = Direction2()
        override var brain: IBrain? = null
        override val upgrades: Map<UpgradeType, Upgrade>
            get() = this@Ear.upgrades
        // </editor-fold>
        // <editor-fold desc="Controllable">
        var unit = UnitTypes.block.create(team) as BlockUnitc
        override fun unit(): Unit {
            //make sure stats are correct
            unit.tile(this)
            unit.team(team)
            return (unit as Unit)
        }
        // </editor-fold>
        // <editor-fold desc="Logic">
        var logicControlTime: Float = -1f
        val logicControlled: Boolean
            get() = logicControlTime > 0
        val logicAim = Vec2()
        override fun control(type: LAccess, p1: Double, p2: Double, p3: Double, p4: Double) {
            if (type == LAccess.shoot && !unit.isPlayer && !p3.isZero) {
                logicControlTime = 60f
                logicAim.set(
                    (p1 * Vars.tilesize).toFloat(),
                    (p2 * Vars.tilesize).toFloat(),
                )
            }
            super.control(type, p1, p2, p3, p4)
        }

        override fun control(type: LAccess, p1: Any?, p2: Double, p3: Double, p4: Double) {
            if (type == LAccess.shootp && !unit.isPlayer && !p2.isZero) {
                if (p1 is Posc) {
                    logicControlTime = 60f
                    logicAim.set(p1.x(), p1.y())
                }
            }
            super.control(type, p1, p2, p3, p4)
        }

        override fun sense(sensor: LAccess): Double {
            return when (sensor) {
                LAccess.shooting -> sonicWaves.list.isNotEmpty().toDouble()
                LAccess.progress -> (reloadCounter / realReloadTime).toDouble()
                else -> super.sense(sensor)
            }
        }
        // </editor-fold>
        // <editor-fold desc="Serialized">
        @Serialized
        var reloadCounter = 0f
        @Serialized
        var lastRadiateTime = realReloadTime + 1f
        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            sonicWaves.read(read)
            reloadCounter = read.f()
            lastRadiateTime = read.f()
        }

        override fun write(write: Writes) {
            super.write(write)
            sonicWaves.write(write)
            write.f(reloadCounter)
            write.f(lastRadiateTime)
        }
        // </editor-fold>
        // <editor-fold desc="Properties">
        val realReloadTime: Float
            get() = reloadTime * (1f - if (isLinkedBrain) reloadTimeI else 0f)
        val realRange: Float
            get() = range * (1f + if (isLinkedBrain) rangeI else 0f)
        val realWaveWidth: Float
            get() = waveWidth * (1f + if (isLinkedBrain) waveWidthI else 0f)
        val realDamage: Float
            get() = damage * (1f + if (isLinkedBrain) damageI else 0f)
        val realSonicRadius: Float
            get() = sonicMaxRadius * (1f + if (isLinkedBrain) sonicMaxRadiusI else 0f)
        val realPowerUse: Float
            get() = powerUse * (1f + if (isLinkedBrain) powerUseI else 0f)
        val realSensitive: Float
            get() = sensitivity * (1f + if (isLinkedBrain) sensitivityI else 0f)
        // </editor-fold>
        // <editor-fold desc="Draw">
        override fun draw() {
            BaseTR.Draw(x, y)
            var scale = 1f
            val scaleDuration = realSonicRadius / waveSpeed
            if (lastRadiateTime <= scaleDuration * 2f) {
                val progress = lastRadiateTime / scaleDuration
                scale += Interp.sine(progress) * maxScale
            }
            EarTR.DrawSize(x, y, scale)
            Draw.z(Layer.bullet)
            for (wave in sonicWaves) {
                val alpha = Interp.pow2Out(wave.range / realSonicRadius)
                Lines.stroke(waveWidth, sonicWaveColor)
                Draw.alpha((1f - alpha) + 0.4f)
                Lines.circle(wave.x, wave.y, wave.range)
            }
        }

        override fun drawSelect() {
            G.dashCircle(x, y, realRange, sonicWaveColor)
        }
        // </editor-fold>
        val sonicWaves = SonicWaveQueue(maxSonicWaveNum)
        override fun updateTile() {
            scale.update()
            lastRadiateTime += Time.delta
            logicControlTime -= Time.delta
            sonicWaves.forEach {
                it.range += waveSpeed * Time.delta
            }
            sonicWaves.pollWhen {
                it.range >= realSonicRadius
            }
            reloadCounter += edelta()
            if (sonicWaves.canAdd) {
                val temp = Tmp.v1
                if (reloadCounter >= realReloadTime) {
                    var radiated = false
                    val player = unit()
                    if (isControlled && player.isShooting) {
                        temp.set(player.aimX - x, player.aimY - y).limit(realRange).add(x, y)
                        sonicWaves.append(
                            SonicWave(0f, temp.x, temp.y, realDamage)
                        )
                        radiated = true
                    } else if (logicControlled) {
                        temp.set(logicAim.x - x, logicAim.y - y).limit(realRange).add(x, y)
                        sonicWaves.append(
                            SonicWave(
                                0f, temp.x, temp.y,
                                realDamage
                            )
                        )
                        radiated = true
                    } else {
                        val result = Units.bestTarget(
                            team, x, y, realRange,
                            { !it.dead() && it.isSensed },
                            { false },
                            UnitSorts.weakest
                        )
                        if (result is Velc) {
                            sonicWaves.append(
                                SonicWave(
                                    0f, result.x(), result.y(),
                                    realDamage * (1f + (result.vel().len() - realSensitive) * bounce)
                                )
                            )
                            radiated = true
                        }
                    }
                    if (radiated) {
                        reloadCounter = 0f
                        lastRadiateTime = 0f
                    }
                }
            }
            val realSonicRadius = realSonicRadius
            val realRangeX2 = (realRange + realSonicRadius) * 2
            val halfWidth = realWaveWidth / 2
            Groups.unit.intersect(
                x - realRange,
                y - realRange,
                realRangeX2,
                realRangeX2
            )
            { unit ->
                if (unit.team != team && !unit.dead) {
                    sonicWaves.forEach {
                        val dst = unit.dst(it.x, it.y)
                        if (dst in it.range - halfWidth..it.range + halfWidth) {
                            unit.damageContinuous(it.damage)
                        }
                    }
                }
            }
        }

        override fun delta(): Float {
            return this.timeScale * Time.delta * speedScale
        }

        val MdtUnit.isSensed: Boolean
            get() = this.vel.len() >= realSensitive

        override fun remove() {
            super.remove()
            clear()
        }

        override fun onProximityRemoved() {
            super.onProximityRemoved()
            clear()
        }
    }
}