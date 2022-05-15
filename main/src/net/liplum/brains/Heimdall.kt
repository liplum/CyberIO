package net.liplum.brains

import arc.audio.Sound
import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Fill
import arc.graphics.g2d.Lines
import arc.math.Interp
import arc.math.Mathf
import arc.math.geom.Intersector
import arc.util.Log
import arc.util.Time
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.Vars
import mindustry.ai.types.CommandAI
import mindustry.content.Fx
import mindustry.content.UnitTypes
import mindustry.gen.*
import mindustry.graphics.Layer
import mindustry.graphics.Pal
import mindustry.logic.LAccess
import mindustry.logic.Ranged
import mindustry.world.Block
import mindustry.world.blocks.ControlBlock
import mindustry.world.meta.Stat
import net.liplum.*
import net.liplum.api.IExecutioner
import net.liplum.api.brain.*
import net.liplum.api.brain.IBrain.Companion.Mirror
import net.liplum.api.brain.IBrain.Companion.XSign
import net.liplum.api.brain.IBrain.Companion.YSign
import net.liplum.lib.animations.anims.Anime
import net.liplum.lib.animations.anims.linearFrames
import net.liplum.lib.delegates.Delegate
import net.liplum.lib.entity.Radiation
import net.liplum.lib.entity.RadiationQueue
import net.liplum.lib.ui.bars.AddBar
import net.liplum.utils.*

/**
 * ### Since 1
 * - Heimdall's force field has stored current radius.
 */
open class Heimdall(name: String) : Block(name) {
    @JvmField var range = 150f
    @JvmField var reloadTime = 60f
    @JvmField var waveSpeed = 2f
    @JvmField var waveWidth = 8f
    @JvmField var damage = 8f
    @JvmField var controlLine = 0.05f
    @JvmField var maxBrainWaveNum = 3
    @JvmField var forceFieldRegen = 3f
    @JvmField var forceFieldMax = 2000f
    @JvmField var forceFieldRadius = 50f
    @JvmField var forceFieldRadiusSpeed = 0.1f
    @JvmField var forceFieldRestoreTime = 200f
    @JvmField var powerUse = 2f
    @ClientOnly lateinit var BuckleTRs: Array<TR>
    @ClientOnly @JvmField var BuckleDuration = 20f
    @ClientOnly @JvmField var BuckleFrameNum = 5
    @JvmField var connectedSound: Sound = Sounds.none
    @JvmField var formationPatterns: MutableSet<IFormationPattern> = HashSet()
    @JvmField var properties = mapOf(
        UT.Damage to damage,
        UT.Range to range,
        UT.WaveSpeed to waveSpeed,
        UT.WaveWidth to waveWidth,
        UT.ReloadTime to reloadTime,
        UT.ControlLine to controlLine,
        UT.ForceFieldMax to forceFieldMax,
        UT.ForceFieldRegen to forceFieldRegen,
        UT.ForceFieldRadius to forceFieldRadius,
        UT.ForceFieldRestoreTime to forceFieldRestoreTime,
        UT.PowerUse to powerUse,
        UT.MaxBrainWaveNum to maxBrainWaveNum.toFloat(),
    )

    init {
        solid = true
        update = true
        hasPower = true
        sync = true
        canOverdrive = false
    }

    override fun init() {
        clipSize = range * 1.5f
        if (size != 4) {
            Log.warn("Block $name's size isn't 4 but $size, so it was set as 4 automatically.")
            size = 4
        }
        consumePowerDynamic<HeimdallBuild> {
            it.realPowerUse
        }
        super.init()
    }

    override fun load() {
        super.load()
        BuckleTRs = this.sheet("buckle", BuckleFrameNum)
    }

    fun addFormationPatterns(vararg patterns: IFormationPattern) {
        for (pattern in patterns)
            formationPatterns.add(pattern)
    }

    override fun setStats() {
        super.setStats()
        stats.addHeimdallProperties(properties)
        stats.remove(Stat.powerUse)
        addPowerUseStats()
    }
    @ClientOnly
    @DebugOnly
    var maxPowerUse = powerUse
    override fun setBars() {
        super.setBars()
        DebugOnly {
            AddBar<HeimdallBuild>(R.Bar.FormationN,
                { "$formationEffects" },
                { R.C.BrainWave },
                { if (formationEffects.isNotEmpty) 1f else 0f }
            )
            AddBar<HeimdallBuild>("shield",
                { "${shieldAmount.toInt()}" },
                { R.C.BrainWave },
                { shieldAmount / realForceFieldMax }
            )
            AddBar<HeimdallBuild>("last-damage-time",
                { lastShieldDamageTime.format(1) },
                { R.C.BrainWave },
                { lastShieldDamageTime / realForceFieldRestoreTime }
            )

            AddBar<HeimdallBuild>("power-use",
                { "PowerUse:${(realPowerUse * 60).toInt()}" },
                { Pal.power },
                {
                    maxPowerUse = maxPowerUse.coerceAtLeast(realPowerUse + 2f)
                    realPowerUse / maxPowerUse
                }
            )
            AddBar<HeimdallBuild>("max-brain-wave",
                { "BrainWaves:${(realMaxBrainWaveNum)}" },
                { Pal.power },
                { realMaxBrainWaveNum / 5f }
            )
        }
    }

    open inner class HeimdallBuild : Building(),
        ControlBlock, IExecutioner, IBrain, Ranged {
        override val sides: Array<Side2> = Array(4) {
            Side2(this)
        }
        val properties: Map<UpgradeType, Prop> = mapOf(
            UT.Damage to Prop(damage, ::realDamage::get, ::realDamage::set),
            UT.Range to Prop(range, ::realRange::get, ::realRange::set),
            UT.WaveSpeed to Prop(waveSpeed, ::realWaveSpeed::get, ::realWaveSpeed::set),
            UT.WaveWidth to Prop(waveWidth, ::realWaveWidth::get, ::realWaveWidth::set),
            UT.ReloadTime to Prop(reloadTime, ::realReloadTime::get, ::realReloadTime::set),
            UT.ControlLine to Prop(controlLine, ::executeProportion::get, ::executeProportion::set),
            UT.ForceFieldMax to Prop(forceFieldMax, ::realForceFieldMax::get, ::realForceFieldMax::set),
            UT.ForceFieldRegen to Prop(forceFieldRegen, ::realForceFieldRegen::get, ::realForceFieldRegen::set),
            UT.ForceFieldRadius to Prop(forceFieldRadius, ::realForceFieldRadius::get, ::realForceFieldRadius::set),
            UT.ForceFieldRestoreTime to Prop(forceFieldRestoreTime, ::realForceFieldRestoreTime::get, ::realForceFieldRestoreTime::set),
            UT.PowerUse to Prop(powerUse, ::realPowerUse::get, ::realPowerUse::set),
            UT.MaxBrainWaveNum to Prop(maxBrainWaveNum.toFloat(),
                getter = { realMaxBrainWaveNum.toFloat() },
                setter = { realMaxBrainWaveNum = it.toInt() }
            )
        )

        override fun range(): Float = realRange
        override val onComponentChanged: Delegate = Delegate()
        override var components: MutableSet<IUpgradeComponent> = HashSet()
        val brainWaves = RadiationQueue { realMaxBrainWaveNum }
        var logicControlTime: Float = -1f
        val logicControlled: Boolean
            get() = logicControlTime > 0
        var unit = UnitTypes.block.create(team) as BlockUnitc
        @Serialized
        var reloadCounter = 0f
        var realRange: Float = range
        var realWaveSpeed: Float = waveSpeed
        var realMaxBrainWaveNum: Int = maxBrainWaveNum
        var realReloadTime: Float = reloadTime
        var realDamage: Float = damage
        var realWaveWidth: Float = waveWidth
        var realForceFieldRegen: Float = forceFieldRegen
        var realForceFieldMax: Float = forceFieldMax
        var realForceFieldRadius: Float = forceFieldRadius
        var realForceFieldRestoreTime: Float = forceFieldRestoreTime
        var realPowerUse: Float = powerUse
        @Serialized
        var lastShieldDamageTime: Float = 0f
            set(value) {
                field = value.coerceAtLeast(0f)
            }
        override var executeProportion: Float = controlLine
        @ClientOnly lateinit var linkAnime: Anime
        override var formationEffects: FormationEffects = FormationEffects.Empty
        @Serialized
        override var shieldAmount: Float = 0f
            set(value) {
                field = value.coerceAtLeast(0f)
            }
        val forcePct: Float
            get() = shieldAmount / realForceFieldMax
        val targetFieldRadius: Float
            get() = realForceFieldRadius * forcePct
        @Serialized
        var curFieldRadius = 0f
            set(value) {
                field = value.coerceAtLeast(0f)
            }

        init {
            ClientOnly {
                linkAnime = Anime(BuckleTRs.linearFrames(BuckleDuration))
            }
        }

        override fun updateTile() {
            // Brain waves
            reloadCounter += edelta()
            brainWaves.forEach {
                it.range += waveSpeed * delta()
            }
            brainWaves.pollWhen {
                it.range >= realRange
            }
            if (reloadCounter >= realReloadTime) {
                reloadCounter = 0f
                if ((isControlled && unit.isShooting) ||
                    logicControlled || enemyNearby()
                ) {
                    if (brainWaves.canAdd)
                        brainWaves.append(Radiation())
                }
            }
            val realRangeX2 = realRange * 2
            val halfWidth = realWaveWidth / 2
            Groups.unit.intersect(
                x - realRange,
                y - realRange,
                realRangeX2,
                realRangeX2
            ) { unit ->
                if (unit.team != team && !unit.dead) {
                    brainWaves.forEach {
                        val dst = unit.dst(this)
                        if (dst in it.range - halfWidth..it.range + halfWidth) {
                            unit.damageContinuous(realDamage)
                            if (unit.canBeExecuted) {
                                val originalTeam = unit.team
                                originalTeam.data().units.remove(unit)
                                if (!team.isAI) {
                                    unit.controller(CommandAI())
                                }
                                unit.team = team
                                team.data().updateCount(unit.type, 1)
                                if (unit.count() > unit.cap() && !unit.spawnedByCore) {
                                    Call.unitCapDeath(unit)
                                    team.data().updateCount(unit.type, -1)
                                }
                                unit.heal()
                            }
                        }
                    }
                }
            }
            lastShieldDamageTime += Time.delta
            // Force field
            if (formationEffects.enableShield) {
                if (shieldAmount < realForceFieldMax) {
                    val factor = if (lastShieldDamageTime > realForceFieldRestoreTime)
                        1f else 0.25f
                    shieldAmount += realForceFieldRegen * edelta() * factor
                }
                curFieldRadius = Mathf.approach(
                    curFieldRadius, targetFieldRadius,
                    edelta() * forceFieldRadiusSpeed
                )
            } else {
                shieldAmount -= realForceFieldRegen * 2f
                curFieldRadius -= 2f * Time.delta
            }
            if (shieldAmount > 0f) {
                Groups.bullet.intersect(
                    x - curFieldRadius,
                    y - curFieldRadius,
                    curFieldRadius * 2f,
                    curFieldRadius * 2f,
                ) {
                    if (absorbBullet(it, curFieldRadius)) {
                        lastShieldDamageTime = 0f
                    }
                }
            }
            // Formation
            formationEffects.update(this)
        }

        override fun remove() {
            super.remove()
            unlinkAll()
        }

        override fun onProximityRemoved() {
            super.onProximityRemoved()
            unlinkAll()
            clear()
        }

        open fun <T> linkComponent(component: T, dire: Direction2): Boolean
                where T : Building, T : IUpgradeComponent {
            if (dire.isClinging) {
                if (dire.onPart0)
                    dire.sideObj[0] = component
                if (dire.onPart1)
                    dire.sideObj[1] = component
                return component.linkBrain(this, dire)
            }
            return false
        }

        override fun onProximityUpdate() {
            super.onProximityUpdate()
            clear()
            for (build in proximity) {
                if (build is IUpgradeComponent &&
                    (build.brain == this || build.canLinked(this))
                ) {
                    if (build.brain == null) {
                        val dire = this.sideOn(build)
                        val succeed = linkComponent(build, dire)
                        if (succeed) {
                            connectedSound.at(tile)
                            ClientOnly {
                                linkAnime.restart()
                            }
                        }
                    } else if (build.brain == this) {
                        linkComponent(build, build.directionInfo)
                    }
                }
            }
            checkFormation()
            recacheUpgrade()
        }

        open fun checkFormation() {
            val res: HashSet<IFormationEffect> = HashSet()
            for (pattern in formationPatterns) {
                val effect = pattern.match(this)
                if (effect != null)
                    res.add(effect)
            }
            formationEffects = FormationEffects(res)
        }

        override fun draw() {
            WhenNotPaused {
                linkAnime.spend(Time.delta)
            }
            super.draw()
            // buckles
            Draw.z(Layer.blockOver)
            val step = size * Vars.tilesize / 4f
            val step2 = step * 2
            for ((i, side) in sides.withIndex()) {
                val rotation = i * 90f
                var dx = step * XSign[i]
                var dy = step * YSign[i]
                val mirror = Mirror[i]
                for ((j, com) in side.components.withIndex()) {
                    val isVertical = i % 2 == 0
                    if (isVertical) { // vertical
                        dy -= j * step2
                    } else { // horizontal
                        dx += j * step2
                    }
                    if (com == null) continue
                    linkAnime.draw(x + dx, y + dy, rotation)
                    if (isVertical) {
                        linkAnime.draw(
                            x + dx + mirror * step2,
                            y + dy,
                            rotation + 180f
                        )
                    } else {
                        linkAnime.draw(
                            x + dx,
                            y + dy + mirror * step2,
                            rotation + 180f
                        )
                    }
                }
            }
            Draw.reset()
            // brain waves
            Draw.z(Layer.bullet)
            val realRange = realRange
            for (wave in brainWaves) {
                val dst = realRange - wave.range
                val alpha = Interp.pow2Out(dst / realRange)
                Lines.stroke(waveWidth, R.C.BrainWave)
                Draw.alpha(alpha + 0.7f)
                Lines.circle(x, y, wave.range)
            }
            Draw.reset()
            // Force field
            if (shieldAmount > 0) {
                Draw.z(Layer.shields)
                if (Vars.renderer.animateShields) {
                    Draw.color(R.C.BrainWave)
                    Fill.poly(x, y, 6, curFieldRadius)
                } else {
                    Draw.color(R.C.BrainWave, Color.white, forcePct.coerceIn(0f, 1f) * 0.5f)
                    Lines.stroke(1.5f)
                    Draw.alpha(0.09f)
                    Fill.poly(x, y, 6, curFieldRadius)
                    Draw.alpha(1f)
                    Lines.poly(x, y, 6, curFieldRadius)
                }
            }
            Draw.reset()
            formationEffects.draw(this)
        }

        open fun enemyNearby(): Boolean {
            for (unit in Groups.unit) {
                if (unit.team != team &&
                    !unit.dead &&
                    unit.dst(this) <= realRange
                ) {
                    return true
                }
            }
            return false
        }

        override fun drawSelect() {
            G.dashCircle(x, y, realRange, R.C.BrainWave)
        }

        override fun canControl() = canConsume()
        override fun unit(): MdtUnit {
            //make sure stats are correct
            unit.tile(this)
            unit.team(team)
            return (unit as MdtUnit)
        }

        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            brainWaves.read(read)
            reloadCounter = read.f()
            shieldAmount = read.f()
            lastShieldDamageTime = read.f()
            curFieldRadius = read.f()
        }

        override fun write(write: Writes) {
            super.write(write)
            brainWaves.write(write)
            write.f(reloadCounter)
            write.f(shieldAmount)
            write.f(lastShieldDamageTime)
            write.f(curFieldRadius)
        }

        override fun control(type: LAccess, p1: Double, p2: Double, p3: Double, p4: Double) {
            if (type == LAccess.shoot && !unit.isPlayer && !p3.isZero) {
                logicControlTime = 60f
            }
            super.control(type, p1, p2, p3, p4)
        }

        override fun control(type: LAccess, p1: Any?, p2: Double, p3: Double, p4: Double) {
            if (type == LAccess.shootp && !unit.isPlayer && !p2.isZero) {
                if (p1 is Posc) {
                    logicControlTime = 60f
                }
            }
            super.control(type, p1, p2, p3, p4)
        }

        protected val deltaUpgradePropMap = HashMap<UpgradeType, UpgradeEntry>(properties.size + 1)
        protected val rateUpgradePropMap = HashMap<UpgradeType, UpgradeEntry>(properties.size + 1)
        fun clearPropMap() {
            deltaUpgradePropMap.clear()
            rateUpgradePropMap.clear()
        }

        open fun recacheUpgrade() {
            if (components.isNotEmpty()) {
                recacheProperties()
            } else {
                for (prop in properties.values) {
                    prop.setter(prop.default)
                }
            }
        }

        open fun recacheProperties() {
            clearPropMap()
            if (components.isEmpty()) {
                for (prop in properties.values) {
                    prop.setter(prop.default)
                }
            } else {
                for (com in components) {
                    for ((type, upgrade) in com.upgrades) {
                        if (upgrade.isDelta) {
                            val entry = deltaUpgradePropMap.getOrPut(type) { UpgradeEntry() }
                            entry.value += upgrade.value
                        } else {
                            val entry = rateUpgradePropMap.getOrPut(type) { UpgradeEntry() }
                            entry.value += upgrade.value
                        }
                    }
                }
                for ((type, effectEntry) in formationEffects.deltaUpgrades) {
                    val entry = deltaUpgradePropMap.getOrPut(type) { UpgradeEntry() }
                    entry.value += effectEntry.value
                }
                for ((type, effectEntry) in formationEffects.rateUpgrades) {
                    val entry = rateUpgradePropMap.getOrPut(type) { UpgradeEntry() }
                    entry.value += effectEntry.value
                }
                for ((type, prop) in properties) {
                    //delta
                    val deltaUpSum = deltaUpgradePropMap[type]
                    var sum = prop.default
                    if (deltaUpSum != null) {
                        sum += deltaUpSum.value
                    }
                    //rate
                    val rateUpSum = rateUpgradePropMap[type]
                    if (rateUpSum != null) {
                        sum *= 1f + rateUpSum.value
                    }
                    sum = sum.coerceAtLeast(0f)
                    if (sum != prop.default)
                        prop.setter(sum)
                }
            }
        }

        fun absorbBullet(bullet: Bullet, range: Float): Boolean {
            if (bullet.team != team &&
                bullet.type.absorbable &&
                bullet.dst(this) <= range &&
                Intersector.isInsideHexagon(x, y, range * 2f, bullet.x, bullet.y)
            ) {
                bullet.absorb()
                Fx.absorb.at(bullet)
                shieldAmount -= bullet.damage
                return true
            }
            return false
        }
    }
}

class Prop(
    val default: Float,
    val getter: () -> Float,
    val setter: (Float) -> Unit,
)