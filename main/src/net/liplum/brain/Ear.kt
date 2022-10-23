package net.liplum.brain

import arc.func.Prov
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Lines
import arc.math.Interp
import arc.math.Mathf
import arc.math.geom.Vec2
import arc.struct.EnumSet
import arc.util.Time
import arc.util.Tmp
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.Vars
import mindustry.content.UnitTypes
import mindustry.entities.TargetPriority
import mindustry.entities.UnitSorts
import mindustry.entities.Units
import mindustry.gen.*
import mindustry.gen.Unit
import mindustry.graphics.Layer
import mindustry.logic.LAccess
import mindustry.logic.Ranged
import mindustry.world.Block
import mindustry.world.blocks.ControlBlock
import mindustry.world.meta.BlockFlag
import mindustry.world.meta.BlockGroup
import mindustry.world.meta.Stat
import mindustry.world.meta.StatUnit
import net.liplum.R
import net.liplum.Var
import net.liplum.api.brain.*
import net.liplum.common.entity.PosRadiation
import net.liplum.common.persistence.ReadFromCache
import net.liplum.common.persistence.WriteIntoCache
import net.liplum.common.util.forLoop
import net.liplum.common.util.toDouble
import net.liplum.input.smoothPlacing
import net.liplum.input.smoothSelect
import plumy.core.Serialized
import plumy.core.assets.TR
import plumy.core.math.isZero
import plumy.core.ClientOnly
import plumy.animation.ContextDraw.Draw
import plumy.animation.ContextDraw.DrawScale
import net.liplum.render.*
import plumy.core.MUnit
import plumy.dsl.TileXY
import net.liplum.utils.sub
import net.liplum.render.G
import net.liplum.render.HeatMeta
import net.liplum.render.drawHeat
import plumy.core.math.invoke
import plumy.dsl.getCenterWorldXY

/**
 * ### Since 1
 * Use CacheReader instead.
 */
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
    @JvmField var range = 150f
    @JvmField var rangeI = 0.4f
    @JvmField var reloadTime = 120f
    @JvmField var reloadTimeI = -0.4f
    @JvmField var powerConsumeTime = 30f
    @JvmField var maxSonicWaveNum = 3
    @JvmField var bounce = 1f
    /**
     * Cooling per tick. It should be multiplied by [Time.delta]
     */
    @JvmField var coolingSpeed = 0.01f
    // Visual effects
    @ClientOnly lateinit var BaseTR: TR
    @ClientOnly lateinit var EarTR: TR
    @ClientOnly lateinit var BaseHeatTR: TR
    @ClientOnly lateinit var EarHeatTR: TR
    @ClientOnly @JvmField var maxSelectedCircleTime = Var.SelectedCircleTime
    @ClientOnly @JvmField val heatMeta = HeatMeta()
    @ClientOnly @JvmField var scaleTime = 30f
    @ClientOnly @JvmField var maxScale = 0.3f
    @ClientOnly @JvmField var sonicWaveColor = R.C.SonicWave
    @ClientOnly @JvmField var sounds = 0.8f..1.2f
    @ClientOnly @JvmField var soundPitchRange = 0.8f..1.2f
    override val upgrades: MutableMap<UpgradeType, Upgrade> = HashMap()

    init {
        solid = true
        update = true
        updateInUnits = true
        alwaysUpdateInUnits = true
        priority = TargetPriority.turret
        group = BlockGroup.turrets
        flags = EnumSet.of(BlockFlag.turret)
        hasPower = true
        sync = true
        attacks = true
        canOverdrive = false
        conductivePower = true
        buildType = Prov { EarBuild() }
    }

    override fun init() {
        checkInit()
        super.init()
        clipSize = (range * (1f + rangeI)) + (sonicMaxRadius * (1f * rangeI))
    }

    override fun load() {
        super.load()
        BaseTR = this.sub("base")
        BaseHeatTR = this.sub("base-heat")
        EarHeatTR = this.sub("heat")
        EarTR = region
    }

    override fun icons() = arrayOf(BaseTR, EarTR)
    override fun setBars() {
        super.setBars()
        addBrainLinkInfo<EarBuild>()
    }

    override fun drawPlace(x: TileXY, y: TileXY, rotation: Int, valid: Boolean) {
        super.drawPlace(x, y, rotation, valid)
        G.dashCircleBreath(
            getCenterWorldXY(x),
            getCenterWorldXY(y),
            range * smoothPlacing(maxSelectedCircleTime),
            sonicWaveColor, stroke = Var.CircleStroke
        )
    }

    override fun setStats() {
        super.setStats()
        this.addUpgradeComponentStats()
        stats.remove(Stat.powerUse)
    }

    open inner class EarBuild : Building(),
        IUpgradeComponent, ControlBlock, Ranged {
        override fun version() = 1.toByte()
        override fun shouldConsume(): Boolean {
            return enabled && lastRadiateTime < powerConsumeTime
        }
        // <editor-fold desc="Heimdall">
        override val componentName = "Ear"
        override val scale: SpeedScale = SpeedScale()
        override var directionInfo: Direction2 = Direction2()
        override var brain: IBrain? = null
        override val upgrades: Map<UpgradeType, Upgrade>
            get() = this@Ear.upgrades
        override var heatShared = 0f
            set(value) {
                field = value.coerceIn(0f, 1f)
            }

        override fun afterPickedUp() {
            super.afterPickedUp()
            unlinkBrain()
        }
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
        val sonicWaves = SonicWaveQueue(maxSonicWaveNum)
        @Serialized
        var reloadCounter = 0f
        @Serialized
        var lastRadiateTime = realReloadTime + 1f
        override fun read(_read_: Reads, revision: Byte) {
            super.read(_read_, revision)
            val version = revision.toInt()
            if (version == 0) {
                _read_.i().forLoop {
                    PosRadiation.readEmpty(_read_)
                }
                reloadCounter = _read_.f()
                lastRadiateTime = _read_.f()
            } else { // Since 1
                ReadFromCache(_read_, version().toInt()) {
                    sonicWaves.read(this.cache)
                    reloadCounter = f()
                    lastRadiateTime = f()
                }
            }
        }

        override fun write(_write_: Writes) {
            super.write(_write_)
            WriteIntoCache(_write_, version().toInt()) {
                sonicWaves.write(this)
                f(reloadCounter)
                f(lastRadiateTime)
            }
        }

        override fun range() = realRange
        // </editor-fold>
        // <editor-fold desc="Properties">
        val realReloadTime: Float
            get() = reloadTime * (1f + if (isLinkedBrain) reloadTimeI else 0f)
        val realRange: Float
            get() = range * (1f + if (isLinkedBrain) rangeI else 0f)
        val realWaveWidth: Float
            get() = waveWidth * (1f + if (isLinkedBrain) waveWidthI else 0f)
        val realDamage: Float
            get() = damage * (1f + if (isLinkedBrain) damageI else 0f)
        val realSonicRadius: Float
            get() = sonicMaxRadius * (1f + if (isLinkedBrain) sonicMaxRadiusI else 0f)
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
            Draw.z(Layer.blockAdditive)
            heatMeta.drawHeat(heatShared) {
                BaseHeatTR.Draw(x, y)
            }
            Draw.z(Layer.turret)
            EarTR.DrawScale(x, y, scale + G.sin / 20f)
            Draw.z(Layer.turretHeat)
            heatMeta.drawHeat(heatShared) {
                EarHeatTR.DrawScale(x, y, scale + G.sin / 20f)
            }
            Draw.z(Layer.turret)
            Draw.z(Layer.bullet)
            for (wave in sonicWaves) {
                val alpha = Interp.pow2In(wave.range / realSonicRadius)
                Lines.stroke(waveWidth * (1f - alpha), sonicWaveColor)
                Lines.circle(wave.x, wave.y, wave.range)
            }
        }

        override fun drawSelect() {
            G.dashCircleBreath(x, y, realRange * smoothSelect(maxSelectedCircleTime), sonicWaveColor, stroke = Var.CircleStroke)
        }

        override fun shouldActiveSound(): Boolean {
            return enabled && sonicWaves.list.isNotEmpty()
        }
        // </editor-fold>
        override fun updateTile() {
            scale.update()
            lastRadiateTime += Time.delta
            logicControlTime -= Time.delta
            heatShared -= coolingSpeed * Time.delta
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
                            if (unit.dead) {
                                if (Mathf.chance(0.3)) {
                                    trigger(Trigger.killing)
                                } else {
                                    if (unit.isFlying)
                                        trigger(Trigger.earKillingFlying)
                                    else
                                        trigger(Trigger.earKilling)
                                }
                            }
                        }
                    }
                }
            }
        }

        override fun heal() {
            super.heal()
            trigger(Trigger.heal)
        }

        override fun heal(amount: Float) {
            super.heal(amount)
            trigger(Trigger.heal)
        }

        override fun delta(): Float {
            return this.timeScale * Time.delta * speedScale * (1f + heatShared)
        }

        val MUnit.isSensed: Boolean
            get() = this.vel.len() >= realSensitive

        override fun remove() {
            super.remove()
            clear()
        }

        override fun onProximityRemoved() {
            super.onProximityRemoved()
            trigger(Trigger.partDestroyed)
            clear()
        }
    }
}