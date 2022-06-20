package net.liplum.blocks.bomb

import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.math.Mathf
import arc.math.geom.Vec2
import arc.scene.ui.Label
import arc.scene.ui.Slider
import arc.scene.ui.TextButton
import arc.scene.ui.layout.Stack
import arc.scene.ui.layout.Table
import arc.util.Time
import mindustry.Vars
import mindustry.content.Fx
import mindustry.entities.Damage
import mindustry.entities.Effect
import mindustry.entities.Units
import mindustry.gen.Building
import mindustry.gen.Iconc
import mindustry.gen.Sounds
import mindustry.gen.Unit
import mindustry.graphics.Drawf
import mindustry.world.Block
import mindustry.world.meta.BlockStatus
import net.liplum.DebugOnly
import net.liplum.R
import net.liplum.Var
import net.liplum.annotations.isOn
import net.liplum.lib.Serialized
import net.liplum.common.utils.bigEndianByte
import net.liplum.common.utils.on
import net.liplum.common.utils.twoBytesToShort
import net.liplum.lib.math.smooth
import net.liplum.mdt.*
import net.liplum.mdt.render.*
import net.liplum.mdt.ui.bars.AddBar
import net.liplum.mdt.utils.subBundle
import net.liplum.mdt.utils.worldXY
import kotlin.math.roundToInt
import kotlin.math.sqrt

open class ZipBomb(name: String) : Block(name) {
    @JvmField var explodeEffect: Effect = Fx.reactorExplosion
    @JvmField var damagePreUnit = 400f
    @JvmField var rangePreUnit = 10f
    @JvmField var shake = 6f
    @JvmField var shakeDuration = 16f
    @JvmField var maxSensitive = 10
    @JvmField var autoDetectTime = 300f
    @JvmField var warningRangeFactor = 2f
    val explosionRange: Float
        get() = rangePreUnit * Vars.tilesize
    val explosionDamage: Float
        get() = damagePreUnit * Vars.tilesize
    @JvmField var circleColor: Color = R.C.RedAlert
    @ClientOnly @JvmField var maxSelectedCircleTime = Var.SelectedCircleTime
    @JvmInline
    value class Command(val value: Int) {
        val isAutoDetect: Boolean get() = value isOn AutoDetectPos
        val configGear: Int get() = value.bigEndianByte

        companion object {
            private const val AutoDetectPos = 0
            fun config(gear: Int): Command =
                Command(twoBytesToShort(gear, 0))

            fun genFull(autoDetect: Boolean, configGear: Int): Int {
                var r = twoBytesToShort(configGear, 0)
                if (autoDetect) r = r on AutoDetectPos
                return r
            }
        }
    }

    init {
        update = true
        destructible = true
        configurable = true
        solid = false
        sync = true
        targetable = false
        saveConfig = true
        rebuildable = false
        canOverdrive = false
        hasShadow = false
        drawDisabled = false
        commandable = true
        teamPassable = true
        config(java.lang.Integer::class.java) { bomb: ZipBombBuild, cmdCode ->
            bomb.handleCommandFromRemote(Command(cmdCode.toInt()))
        }
        config(java.lang.Boolean::class.java) { bomb: ZipBombBuild, trigger ->
            if (trigger.booleanValue())
                bomb.handleTriggerFromRemote()
        }
    }

    override fun init() {
        maxSensitive = maxSensitive.coerceIn(1, Byte.MAX_VALUE.toInt())
        super.init()
    }

    override fun drawPlace(x: Int, y: Int, rotation: Int, valid: Boolean) {
        super.drawPlace(x, y, rotation, valid)
        G.dashCircleBreath(this, x, y, explosionRange * smoothPlacing(maxSelectedCircleTime), circleColor)
    }

    override fun setBars() {
        super.setBars()
        DebugOnly {
            AddBar<ZipBombBuild>("reload",
                { "Reload:${autoDetectCounter.toInt()}" },
                { circleColor },
                { autoDetectCounter / autoDetectTime }
            )
        }
    }

    val tmp = ArrayList<Unit>()

    open inner class ZipBombBuild : Building() {
        // TODO: Serialization
        @Serialized
        var autoDetectEnabled = true
        @Serialized
        var curSensitive = 1
        @Serialized
        var autoDetectCounter = 0f
        override fun updateTile() {
            HeadlessOnly {
                // Headless don't need to check enemy nearby every tick
                if (!autoDetectEnabled) return
                autoDetectCounter += Time.delta
                if (autoDetectCounter >= autoDetectTime) {
                    autoDetectCounter %= autoDetectTime
                    if (countEnemyNearby() >= curSensitive) {
                        triggerSync()
                    }
                }
            }
            ClientOnly {
                // Client should check enemy nearby every tick for animation
                if (autoDetectEnabled)
                    autoDetectCounter += Time.delta
                // If auto-detect is charging, just detect the enemy without trigger
                if (autoDetectCounter >= autoDetectTime) {
                    autoDetectCounter %= autoDetectTime
                    if (countEnemyNearby() >= curSensitive) {
                        triggerSync()
                    }
                } else {
                    countEnemyNearby(explosionRange * warningRangeFactor)
                }
            }
        }

        var nearestEnemyDst2: Float? = null
        /**
         * ### Side effects:
         * 1. set the [nearestEnemyDst2] as the distance between the nearest enemy
         * @return how many enemies nearby
         */
        open fun countEnemyNearby(range: Float = explosionRange): Int {
            tmp.clear()
            Units.nearbyEnemies(team, x, y, range) {
                tmp.add(it)
            }
            nearestEnemyDst2 = tmp.minOfOrNull { it.dst2(this) }
            return tmp.size
        }

        override fun dropped() {
            triggerSync()
        }

        override fun unitOn(unit: Unit) {
            if (unit.team != team) {
                kill()
            }
        }

        open fun detonate() {
            Sounds.explosionbig.at(this)
            Effect.shake(shake, shakeDuration, x, y)
            Damage.damage(
                team, x, y,
                explosionRange, explosionDamage
            )
            Fx.dynamicExplosion.at(x, y, sqrt(explosionRange))
        }

        override fun drawSelect() {
            super.drawSelect()
            G.dashCircleBreath(x, y, explosionRange * smoothSelect(maxSelectedCircleTime), circleColor)
        }

        override fun onCommand(target: Vec2) {
            triggerSync()
        }

        override fun onDestroyed() {
            super.onDestroyed()
            detonate()
        }
        @ClientOnly
        override fun buildConfiguration(table: Table) {
            table.bottom()
            if (maxSensitive > 1) {
                val pre = 1f / maxSensitive
                table.add(Stack(
                    Slider(pre, 1f, pre, false).apply {
                        value = curSensitive * pre
                        moved { configGearSync((it * (maxSensitive + 1)).toInt().coerceIn(1, maxSensitive)) }
                        update { isDisabled = !autoDetectEnabled }
                    },
                    Table().apply {
                        add(Label { ">=$curSensitive" }.apply {
                            color.set(Color.white)
                        })
                        defaults().center()
                    }
                )
                ).width(250f).row()
            }
            table.add(TextButton("").apply {
                label.setText { getCurrentAutoDetectButton() }
                changed { switchAutoDetectSync() }
            }).width(250f).grow()
            table.defaults().growX()
        }
        @ClientOnly
        fun getCurrentAutoDetectButton(): String {
            return subBundle(
                "auto-detect", if (autoDetectEnabled) Iconc.ok
                else Iconc.cancel
            )
        }

        override fun draw() {
            val dst2 = nearestEnemyDst2
            val progress = if (dst2 != null) (1f - (sqrt(dst2) / (explosionRange * warningRangeFactor)).coerceIn(0f, 1f)).smooth else 0f
            val breathWeak = if (dst2 != null)
                (11f - 10f * progress).roundToInt().toFloat()
            else 18f
            val scl = Mathf.absin(Time.time, breathWeak, 1f) / 8f
            WhenTheSameTeam {
                // only players in the same team can find this
                Drawf.shadow(x, y, size.worldXY * 1.5f)
                block.region.DrawSize(x, y, 1f + scl, drawrot())
            }.Else {
                if (dst2 != null) {
                    Drawf.shadow(x, y, size.worldXY * 1.5f, progress)
                    Draw.alpha(progress)
                    block.region.DrawSize(x, y, 1f + scl, drawrot())
                }
            }
            DebugOnly {
                G.dashCircleBreath(x, y, explosionRange * warningRangeFactor, circleColor)
                G.dashCircleBreath(x, y, explosionRange, circleColor)
            }
        }
        @CalledBySync
        open fun onTrigger() {
            if (isAdded)
                kill()
        }

        override fun config(): Any? {
            return Command.genFull(autoDetectEnabled, curSensitive)
        }
        @CalledBySync
        fun handleCommandFromRemote(cmdCode: Command) {
            autoDetectEnabled = cmdCode.isAutoDetect
            handleConfig(cmdCode.configGear)
        }
        @CalledBySync
        fun handleTriggerFromRemote() {
            onTrigger()
        }
        @CalledBySync
        fun handleConfig(int: Int) {
            curSensitive = int
        }
        @SendDataPack
        open fun triggerSync() {
            if (isAdded)
                configureAny(true)
        }
        @SendDataPack
        open fun configGearSync(gear: Int) {
            configure(Command.genFull(autoDetectEnabled, gear))
        }

        override fun displayable(): Boolean {
            return team() == Vars.player.team()
        }
        @SendDataPack
        open fun switchAutoDetectSync() {
            configure(Command.genFull(!autoDetectEnabled, curSensitive))
        }

        override fun drawTeamTop() {
            WhenTheSameTeam {
                super.drawTeamTop()
            }
        }

        override fun status(): BlockStatus {
            return BlockStatus.active
        }

        override fun drawStatus() {
            WhenTheSameTeam {
                super.drawStatus()
            }
        }

        override fun drawTeam() {
            WhenTheSameTeam {
                super.drawTeam()
            }
        }

        override fun drawCracks() {
        }
    }
}