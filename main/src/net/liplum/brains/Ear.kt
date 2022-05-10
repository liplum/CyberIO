package net.liplum.brains

import arc.graphics.g2d.Draw
import arc.graphics.g2d.Lines
import arc.math.Interp
import arc.math.geom.Vec2
import arc.util.Time
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.content.UnitTypes
import mindustry.entities.UnitSorts
import mindustry.entities.Units
import mindustry.gen.*
import mindustry.gen.Unit
import mindustry.graphics.Layer
import mindustry.logic.LAccess
import mindustry.world.Block
import mindustry.world.blocks.ControlBlock
import mindustry.world.meta.Stat
import mindustry.world.meta.StatUnit
import net.liplum.ClientOnly
import net.liplum.DebugOnly
import net.liplum.R
import net.liplum.api.brain.*
import net.liplum.lib.Draw
import net.liplum.lib.DrawSize
import net.liplum.lib.entity.PosRadiation
import net.liplum.lib.entity.PosRadiationQueue
import net.liplum.utils.*

open class Ear(name: String) : Block(name), IComponentBlock {
    @ClientOnly lateinit var BaseTR: TR
    @ClientOnly lateinit var EarTR: TR
    @JvmField var range = 150f
    @JvmField var reloadTime = 60f
    @JvmField var waveSpeed = 2.5f
    @JvmField var waveWidth = 5f
    @JvmField var damage = 3.2f
    @JvmField var maxSonicWaveNum = 3
    @JvmField var sonicMaxRadius = 40f
    @ClientOnly @JvmField var maxScale = 0.3f
    @ClientOnly @JvmField var scaleTime = 30f
    @JvmField var powerUse = 2f
    @JvmField var sensitivity = 1f
    @JvmField var reloadTimeI = 0.5f
    @JvmField var rangeI = 0.4f
    @JvmField var damageI = 1.5f
    @JvmField var radiusI = 0.4f
    @JvmField var widthI = 0.3f
    @JvmField var powerI = 0.8f
    @JvmField var sensitivityI = -0.3f
    @JvmField var powerConsumeTime = 30f
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

    override fun setStats() {
        super.setStats()
        this.addUpgradeComponentStats()
        stats.remove(Stat.powerUse)
        stats.add(Stat.powerUse, powerUse * 60f, StatUnit.powerSecond)
    }

    open inner class EarBuild : Building(), IUpgradeComponent, ControlBlock {
        override var directionInfo: Direction2 = Direction2()
        override var brain: IBrain? = null
        override val upgrades: Map<UpgradeType, Upgrade>
            get() = this@Ear.upgrades
        val sonicWaves = PosRadiationQueue(maxSonicWaveNum)
        var unit = UnitTypes.block.create(team) as BlockUnitc
        var logicControlTime: Float = -1f
        val logicControlled: Boolean
            get() = logicControlTime > 0
        var reload = 0f
        val realReloadTime: Float
            get() = reloadTime * (1f - if (isLinkedBrain) reloadTimeI else 0f)
        val realRange: Float
            get() = range * (1f + if (isLinkedBrain) rangeI else 0f)
        val realWaveWidth: Float
            get() = waveWidth * (1f + if (isLinkedBrain) widthI else 0f)
        val realDamage: Float
            get() = damage * (1f + if (isLinkedBrain) damageI else 0f)
        val realSonicRadius: Float
            get() = sonicMaxRadius * (1f + if (isLinkedBrain) radiusI else 0f)
        val realPowerUse: Float
            get() = powerUse * (1f + if (isLinkedBrain) powerI else 0f)
        val realSensitive: Float
            get() = sensitivity * (1f + if (isLinkedBrain) sensitivityI else 0f)
        var lastRadiateTime = powerConsumeTime + 1f
        val temp = Vec2()
        override fun updateTile() {
            lastRadiateTime += Time.delta
            sonicWaves.forEach {
                it.range += waveSpeed * delta()
            }
            sonicWaves.pollWhen {
                it.range >= realSonicRadius
            }
            reload += edelta()
            if (sonicWaves.canAdd) {
                //TODO: in v4, the damage of sonic wave depends on the speed of unit sensed.
                if (reload >= realReloadTime) {
                    val player = unit()
                    if (isControlled && player.isShooting) {
                        reload = 0f
                        temp.set(player.aimX - x, player.aimY - y).limit(realRange).add(x, y)
                        sonicWaves.append(
                            PosRadiation(0f, temp.x, temp.y)
                        )
                        lastRadiateTime = 0f
                    } else {
                        val result = Units.bestTarget(
                            team, x, y, realRange,
                            { !it.dead() && it.isSensed },
                            { false },
                            UnitSorts.weakest
                        )
                        if (result != null) {
                            reload = 0f
                            sonicWaves.append(
                                PosRadiation(
                                    0f, result.x, result.y
                                )
                            )
                            lastRadiateTime = 0f
                        }
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
            ) { unit ->
                if (unit.team != team && !unit.dead) {
                    sonicWaves.forEach {
                        val dst = unit.dst(it.x, it.y)
                        if (dst in it.range - halfWidth..it.range + halfWidth) {
                            unit.damageContinuous(realDamage)
                        }
                    }
                }
            }
        }

        val MdtUnit.isSensed: Boolean
            get() = this.vel.len() >= realSensitive

        override fun draw() {
            BaseTR.Draw(x, y)
            var scale = 1f
            val realReloadTime = realReloadTime
            if (lastRadiateTime <= realReloadTime) {
                val progress = lastRadiateTime / realReloadTime
                scale += Interp.sine(progress) * maxScale
            }
            EarTR.DrawSize(x, y, scale)
            Draw.z(Layer.power)
            for (wave in sonicWaves) {
                val alpha = Interp.pow2Out(wave.range / realSonicRadius)
                Lines.stroke(waveWidth, R.C.SonicWave)
                Draw.alpha(1f - alpha)
                Lines.circle(wave.x, wave.y, wave.range)
            }
        }

        override fun drawSelect() {
            G.dashCircle(x, y, realRange, R.C.SonicWave)
        }

        override fun unit(): Unit {
            //make sure stats are correct
            unit.tile(this)
            unit.team(team)
            return (unit as Unit)
        }

        override fun remove() {
            super.remove()
            clear()
        }

        override fun onProximityRemoved() {
            super.onProximityRemoved()
            clear()
        }

        override fun control(type: LAccess, p1: Double, p2: Double, p3: Double, p4: Double) {
            if (type == LAccess.shoot && !unit.isPlayer) {
                logicControlTime = 60f
            }
            super.control(type, p1, p2, p3, p4)
        }

        override fun control(type: LAccess, p1: Any?, p2: Double, p3: Double, p4: Double) {
            if (type == LAccess.shootp && !unit.isPlayer) {
                if (p1 is Posc) {
                    logicControlTime = 60f
                }
            }
            super.control(type, p1, p2, p3, p4)
        }

        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            sonicWaves.read(read)
        }

        override fun write(write: Writes) {
            super.write(write)
            sonicWaves.write(write)
        }
    }
}