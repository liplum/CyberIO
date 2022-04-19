package net.liplum.brains

import arc.audio.Sound
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Lines
import arc.math.Interp
import arc.util.Log
import arc.util.Time
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.Vars
import mindustry.ai.types.SuicideAI
import mindustry.content.UnitTypes
import mindustry.gen.*
import mindustry.graphics.Layer
import mindustry.logic.LAccess
import mindustry.logic.Ranged
import mindustry.world.Block
import mindustry.world.blocks.ControlBlock
import net.liplum.ClientOnly
import net.liplum.R
import net.liplum.WhenNotPaused
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
import net.liplum.utils.*
import java.util.*

open class Heimdall(name: String) : Block(name) {
    @JvmField var range = 150f
    @JvmField var reloadTime = 60f
    @JvmField var waveSpeed = 2f
    @JvmField var waveWidth = 8f
    @JvmField var damage = 8f
    @JvmField var controlLine: Float = 0.05f
    @JvmField var maxBrainWaveNum = 3
    @ClientOnly lateinit var BuckleTRs: Array<TR>
    @ClientOnly @JvmField var BuckleDuration = 20f
    @ClientOnly @JvmField var BuckleFrameNum = 5
    @JvmField var connectedSound: Sound = Sounds.none
    @JvmField var formationPatterns: MutableSet<IFormationPattern> = HashSet()

    init {
        solid = true
        update = true
        hasPower = true
        sync = true
    }

    override fun init() {
        clipSize = range * 1.5f
        if (size != 4) {
            Log.warn("Block $name's size isn't 4 but $size, so it was set as 4 automatically.")
            size = 4
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

    open inner class HeimdallBuild : Building(),
        ControlBlock, IExecutioner, IBrain, Ranged {
        override val sides: Array<Side2> = Array(4) {
            Side2(this)
        }
        val properties: Map<UpgradeType, Prop> = mapOf(
            UpgradeType.Damage to Prop(damage, this::realDamage::get, this::realDamage::set),
            UpgradeType.Range to Prop(range, this::realRange::get, this::realRange::set),
            UpgradeType.WaveSpeed to Prop(waveSpeed, this::realWaveSpeed::get, this::realWaveSpeed::set),
            UpgradeType.WaveWidth to Prop(waveWidth, this::realWaveWidth::get, this::realWaveWidth::set),
            UpgradeType.ReloadTime to Prop(reloadTime, this::realReloadTime::get, this::realReloadTime::set),
            UpgradeType.ControlLine to Prop(controlLine, this::executeProportion::get, this::executeProportion::set),
        )

        override fun range(): Float = realRange
        override val onComponentChanged: Delegate = Delegate()
        override var components: MutableSet<IUpgradeComponent> = HashSet()
        val brainWaves = RadiationQueue { realMaxBrainWaveNum }
        var logicControlTime: Float = -1f
        val logicControlled: Boolean
            get() = logicControlTime > 0
        var unit = UnitTypes.block.create(team) as BlockUnitc
        var reload = 0f
        var realRange: Float = range
        var realWaveSpeed: Float = waveSpeed
        var realMaxBrainWaveNum: Int = maxBrainWaveNum
        var realReloadTime: Float = reloadTime
        var realDamage: Float = damage
        var realWaveWidth: Float = waveWidth
        override var executeProportion: Float = controlLine
        @ClientOnly lateinit var linkAnime: Anime
        var formationEffect: IFormationEffect? = null

        init {
            ClientOnly {
                linkAnime = Anime(BuckleTRs.linearFrames(BuckleDuration))
            }
        }

        override fun updateTile() {
            reload += edelta()
            brainWaves.forEach {
                it.range += waveSpeed * delta()
            }
            brainWaves.pollWhen {
                it.range >= realRange
            }
            if (reload >= realReloadTime) {
                reload = 0f
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
                                unit.team = team
                                unit.controller(SuicideAI())
                                unit.heal()
                            }
                        }
                    }
                }
            }
            formationEffect?.update(sides)
        }

        override fun remove() {
            super.remove()
            unlinkAll()
            clear()
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
            recacheUpgrade()
            checkFormation()
        }

        open fun checkFormation() {
            var res: IFormationEffect? = null
            for (pattern in formationPatterns) {
                res = pattern.match(sides)
                if (res != null)
                    break
            }
            formationEffect = res
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
            // brain waves
            Draw.z(Layer.power)
            val realRange = realRange
            for (wave in brainWaves) {
                val dst = realRange - wave.range
                val alpha = Interp.pow2Out(dst / realRange)
                Lines.stroke(waveWidth, R.C.BrainWave)
                Draw.alpha(alpha)
                Lines.circle(x, y, wave.range)
            }
            formationEffect?.draw(sides)
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

        override fun canControl() = consValid()
        override fun unit(): MdtUnit {
            //make sure stats are correct
            unit.tile(this)
            unit.team(team)
            return (unit as MdtUnit)
        }

        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            brainWaves.read(read)
        }

        override fun write(write: Writes) {
            super.write(write)
            brainWaves.write(write)
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

        protected val deltaUpgradePropMap = HashMap<UpgradeType, MutableList<Upgrade>>(properties.size + 1)
        protected val rateUpgradePropMap = HashMap<UpgradeType, MutableList<Upgrade>>(properties.size + 1)
        fun clearPropMap() {
            deltaUpgradePropMap.clearList()
            rateUpgradePropMap.clearList()
        }

        open fun recacheUpgrade() {
            recacheProperties()
            recacheSpecial()
        }
        /**
         * Used to recache:
         * - [realDamage]
         * - [realRange]
         * - [realWaveSpeed]
         * - [realReloadTime]
         * - [realWaveWidth]
         */
        open fun recacheProperties() {
            clearPropMap()
            for (com in components) {
                for ((type, upgrade) in com.upgrades) {
                    if (upgrade.isDelta)
                        deltaUpgradePropMap[type] = upgrade
                    else
                        rateUpgradePropMap[type] = upgrade
                }
            }
            for ((type, prop) in properties) {
                //delta
                val deltaUps = deltaUpgradePropMap.getSafe(type)
                var sum = prop.default
                for (upgrade in deltaUps) {
                    sum += upgrade.value
                }
                //rate
                val rateUps = rateUpgradePropMap.getSafe(type)
                var rate = 0f
                for (upgrade in rateUps) {
                    rate += upgrade.value
                }
                val res = sum * (1f + rate)
                if (res != prop.default)
                    prop.setter(res)
            }
        }
        /**
         * Used to recache:
         * - [realMaxBrainWaveNum]
         */
        open fun recacheSpecial() {
        }
    }
}

operator fun <K, V> MutableMap<K, MutableList<V>>.set(type: K, upgrade: V) {
    val list = this.computeIfAbsent(type) { LinkedList() }
    list.add(upgrade)
}

fun <K, V> MutableMap<K, MutableList<V>>.clearList() {
    for (list in values) {
        list.clear()
    }
}

fun <K, V> MutableMap<K, MutableList<V>>.getSafe(type: K): List<V> {
    return this[type] ?: emptyList()
}

class Prop(
    val default: Float,
    val getter: () -> Float,
    val setter: (Float) -> Unit
)