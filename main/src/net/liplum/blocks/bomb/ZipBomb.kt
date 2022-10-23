package net.liplum.blocks.bomb

import arc.func.Prov
import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.math.Mathf
import arc.math.geom.Vec2
import arc.scene.ui.Label
import arc.scene.ui.Slider
import arc.scene.ui.layout.Stack
import arc.scene.ui.layout.Table
import arc.util.Time
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.Vars
import mindustry.content.Fx
import mindustry.entities.Damage
import mindustry.entities.Effect
import mindustry.entities.Units
import mindustry.gen.Building
import mindustry.gen.Sounds
import mindustry.gen.Unit
import mindustry.graphics.Drawf
import mindustry.world.Block
import mindustry.world.meta.BlockStatus
import mindustry.world.meta.Stat
import net.liplum.DebugOnly
import net.liplum.R
import net.liplum.Var
import net.liplum.input.smoothPlacing
import net.liplum.input.smoothSelect
import net.liplum.render.G
import net.liplum.utils.CalledBySync
import net.liplum.utils.SendDataPack
import net.liplum.utils.WhenTheSameTeam
import plumy.animation.ContextDraw.DrawScale
import plumy.core.ClientOnly
import plumy.core.Else
import plumy.core.Serialized
import plumy.core.math.smooth
import plumy.dsl.AddBar
import plumy.dsl.config
import plumy.dsl.worldXY
import kotlin.math.roundToInt
import kotlin.math.sqrt

open class ZipBomb(name: String) : Block(name) {
    @JvmField var explodeEffect: Effect = Fx.dynamicExplosion
    @JvmField var shake = 6f
    @JvmField var shakeDuration = 16f
    @JvmField var maxSensitive = 10
    @JvmField var autoDetectTime = 240f
    @JvmField var warningRangeFactor = 2f
    @JvmField var explosionRange = 10f * Vars.tilesize
    @JvmField var explosionDamage = 150f * Vars.tilesize
    @JvmField var circleColor: Color = R.C.RedAlert
    @ClientOnly @JvmField var maxSelectedCircleTime = Var.SelectedCircleTime

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
        buildType = Prov { ZipBombBuild() }
        config<ZipBombBuild, Int> {
            setSensitiveFromRemote(it)
        }
        config<ZipBombBuild, Boolean> {
            if (it) handleTriggerFromRemote()
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

    override fun setStats() {
        super.setStats()
        stats.add(Stat.damage, explosionDamage)
        stats.add(Stat.range, explosionRange)
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
        @Serialized
        var curSensitive = 1
        @Serialized
        var autoDetectCounter = 0f
        override fun updateTile() {
            autoDetectCounter += Time.delta
            if (autoDetectCounter >= autoDetectTime) {
                autoDetectCounter %= autoDetectTime
                if (countEnemyNearby() >= curSensitive) {
                    triggerSync()
                }
            } else {
                // Client should check enemy nearby every tick for animation
                // Headless don't need to check enemy nearby every tick
                ClientOnly {
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
            explodeEffect.at(x, y, sqrt(explosionRange))
        }

        override fun killed() {
            super.killed()
            detonate()
        }

        override fun drawSelect() {
            super.drawSelect()
            G.dashCircleBreath(x, y, explosionRange * smoothSelect(maxSelectedCircleTime), circleColor)
        }

        override fun onCommand(target: Vec2) {
            kill()
        }
        @ClientOnly
        override fun buildConfiguration(table: Table) {
            table.bottom()
            if (maxSensitive > 1) {
                val pre = 1f / maxSensitive
                table.add(Stack(
                    Slider(pre, 1f, pre, false).apply {
                        value = curSensitive * pre
                        moved { configSensitiveSync((it * (maxSensitive + 1)).toInt().coerceIn(1, maxSensitive)) }
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
            table.defaults().growX()
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
                block.region.DrawScale(x, y, 1f + scl, drawrot())
            }.Else {
                if (dst2 != null) {
                    Drawf.shadow(x, y, size.worldXY * 1.5f, progress)
                    Draw.alpha(progress)
                    block.region.DrawScale(x, y, 1f + scl, drawrot())
                }
            }
            DebugOnly {
                G.dashCircleBreath(x, y, explosionRange * warningRangeFactor, circleColor)
                G.dashCircleBreath(x, y, explosionRange, circleColor)
            }
        }

        override fun config(): Any? {
            return curSensitive
        }
        @CalledBySync
        fun setSensitiveFromRemote(gear: Int) {
            curSensitive = gear
        }
        @CalledBySync
        fun handleTriggerFromRemote() {
            kill()
        }
        @SendDataPack
        open fun triggerSync() {
            configureAny(true)
        }
        @SendDataPack
        open fun configSensitiveSync(gear: Int) {
            configure(gear)
        }

        override fun displayable(): Boolean {
            return team() == Vars.player.team()
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

        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            curSensitive = read.i()
            autoDetectCounter = read.f()
        }

        override fun write(write: Writes) {
            super.write(write)
            write.i(curSensitive)
            write.f(autoDetectCounter)
        }
    }
}