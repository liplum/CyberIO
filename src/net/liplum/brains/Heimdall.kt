package net.liplum.brains

import arc.audio.Sound
import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Fill
import arc.graphics.g2d.Lines
import arc.math.Interp
import arc.math.geom.Intersector
import arc.util.Log
import arc.util.Time
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.Vars
import mindustry.ai.types.SuicideAI
import mindustry.content.Fx
import mindustry.content.UnitTypes
import mindustry.gen.*
import mindustry.graphics.Layer
import mindustry.logic.LAccess
import mindustry.logic.Ranged
import mindustry.ui.Bar
import mindustry.world.Block
import mindustry.world.blocks.ControlBlock
import net.liplum.ClientOnly
import net.liplum.DebugOnly
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
    @JvmField var controlLine = 0.05f
    @JvmField var maxBrainWaveNum = 3
    @JvmField var forceFieldRegen = 5f
    @JvmField var forceFieldMax = 2000f
    @JvmField var forceFieldRadius = 50f
    @JvmField var forceFieldCoolDown = 240f
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

    override fun setBars() {
        super.setBars()
        DebugOnly {
            bars.add<HeimdallBuild>(R.Bar.FormationN) {
                Bar(
                    { "${it.formationEffects}" },
                    { R.C.BrainWave },
                    { if (it.formationEffects.isNotEmpty) 1f else 0f }
                )
            }
        }
    }

    open inner class HeimdallBuild : Building(),
        ControlBlock, IExecutioner, IBrain, Ranged {
        override val sides: Array<Side2> = Array(4) {
            Side2(this)
        }
        val properties: Map<UpgradeType, Prop> = mapOf(
            UpgradeType.Damage to Prop(damage, ::realDamage::get, ::realDamage::set),
            UpgradeType.Range to Prop(range, ::realRange::get, ::realRange::set),
            UpgradeType.WaveSpeed to Prop(waveSpeed, ::realWaveSpeed::get, ::realWaveSpeed::set),
            UpgradeType.WaveWidth to Prop(waveWidth, ::realWaveWidth::get, ::realWaveWidth::set),
            UpgradeType.ReloadTime to Prop(reloadTime, ::realReloadTime::get, ::realReloadTime::set),
            UpgradeType.ControlLine to Prop(controlLine, ::executeProportion::get, ::executeProportion::set),
            UpgradeType.ForceFieldMax to Prop(forceFieldMax, ::realForceFieldMax::get, ::realForceFieldMax::set),
            UpgradeType.ForceFieldRegen to Prop(forceFieldRegen, ::realForceFieldRegen::get, ::realForceFieldRegen::set),
            UpgradeType.ForceFieldRadius to Prop(forceFieldRadius, ::realForceFieldRadius::get, ::realForceFieldRadius::set),
            UpgradeType.ForceFieldCoolDown to Prop(forceFieldCoolDown, ::realForceFieldCoolDown::get, ::realForceFieldCoolDown::set),
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
        var realForceFieldRegen: Float = forceFieldRegen
        var realForceFieldMax: Float = forceFieldMax
        var realForceFieldRadius: Float = forceFieldRadius
        var realForceFieldCoolDown: Float = forceFieldCoolDown
        var shieldCoolDown: Float = 0f
            set(value) {
                field = value.coerceAtLeast(0f)
            }
        override var executeProportion: Float = controlLine
        @ClientOnly lateinit var linkAnime: Anime
        override var formationEffects: FormationEffects = FormationEffects.Empty
        override var shieldAmount: Float = 0f
            set(value) {
                field = value.coerceAtLeast(0f)
            }
        val forcePct: Float
            get() = shieldAmount / realForceFieldMax
        val curFieldRadius: Float
            get() = realForceFieldRadius * forcePct

        init {
            ClientOnly {
                linkAnime = Anime(BuckleTRs.linearFrames(BuckleDuration))
            }
        }

        override fun updateTile() {
            // Brain waves
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
            // Force field
            shieldCoolDown -= delta()
            if (!formationEffects.enableShield) {
                shieldAmount -= realForceFieldRegen * 2f
            }
            val fieldExist = shieldCoolDown <= 0f
            if (formationEffects.enableShield &&
                fieldExist &&
                shieldAmount < forceFieldMax
            ) {
                shieldAmount += realForceFieldRegen * power.status
            }
            if (fieldExist && shieldAmount > 0) {
                val curFieldRadius = curFieldRadius
                Groups.bullet.intersect(
                    unit.x - curFieldRadius,
                    unit.y - curFieldRadius,
                    curFieldRadius * 2f,
                    curFieldRadius * 2f,
                ) {
                    absorbBullet(it)
                }
                if (shieldAmount <= 0) {
                    shieldCoolDown = realForceFieldCoolDown
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
            recacheUpgrade()
            checkFormation()
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
            Draw.z(Layer.power)
            val realRange = realRange
            for (wave in brainWaves) {
                val dst = realRange - wave.range
                val alpha = Interp.pow2Out(dst / realRange)
                Lines.stroke(waveWidth, R.C.BrainWave)
                Draw.alpha(alpha)
                Lines.circle(x, y, wave.range)
            }
            Draw.reset()
            // Force field
            val curFieldRadius = curFieldRadius
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
            reload = read.f()
            shieldAmount = read.f()
            shieldCoolDown = read.f()
        }

        override fun write(write: Writes) {
            super.write(write)
            brainWaves.write(write)
            write.f(reload)
            write.f(shieldAmount)
            write.f(shieldCoolDown)
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

        fun absorbBullet(bullet: Bullet): Boolean {
            if (bullet.team != team &&
                bullet.type.absorbable &&
                bullet.dst(this) <= realRange &&
                Intersector.isInsideHexagon(x, y, realRange * 2f, bullet.x, bullet.y)
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