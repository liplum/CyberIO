package net.liplum.brains

import arc.graphics.g2d.Draw
import arc.graphics.g2d.Lines
import arc.math.Interp
import mindustry.content.UnitTypes
import mindustry.gen.BlockUnitc
import mindustry.gen.Building
import mindustry.gen.Groups
import mindustry.gen.Unit
import mindustry.graphics.Layer
import mindustry.world.Block
import mindustry.world.blocks.ControlBlock
import net.liplum.R
import net.liplum.api.IExecutioner
import net.liplum.api.Radiation
import net.liplum.api.RadiationQueue
import net.liplum.utils.G
import net.liplum.utils.healthPct
import net.liplum.utils.invoke

open class Heimdall(name: String) : Block(name), IExecutioner {
    @JvmField var range = 150f
    @JvmField var reloadTime = 10f
    @JvmField var waveSpeed = 2f
    @JvmField var waveWidth = 8f
    @JvmField var damage = 8f
    override var executeProportion: Float = 0.1f

    init {
        solid = true
        update = true
        hasPower = true
    }

    open inner class HeimdallBuild : Building(), ControlBlock, IExecutioner by this@Heimdall {
        val brainWaves = RadiationQueue()
        var logicControlTime: Float = -1f
        val logicControlled: Boolean
            get() = logicControlTime > 0
        var unit = UnitTypes.block.create(team) as BlockUnitc
        var reload = 0f
        val realRange: Float
            get() = range
        val realWaveSpeed: Float
            get() = waveSpeed

        override fun updateTile() {
            reload += edelta()
            for (wave in brainWaves) {
                wave.range += waveSpeed * delta()
            }
            brainWaves.pollWhen {
                it.range >= realRange
            }
            if (reload >= reloadTime) {
                reload = 0f
                if ((isControlled && unit.isShooting) ||
                    logicControlled || enemyNearby()
                ) {
                    if (brainWaves.canAdd)
                        brainWaves.append(Radiation())
                }
            }
            val realRangeX2 = realRange * 2
            val halfWidth = waveWidth / 2
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
                            unit.damageContinuous(damage)
                            if (unit.healthPct <= executeProportion) {
                                unit.team = team
                                unit.heal()
                            }
                        }
                    }
                }
            }
        }

        override fun draw() {
            super.draw()
            Draw.z(Layer.power)
            val realRange = realRange
            for (wave in brainWaves) {
                val dst = realRange - wave.range
                val alpha = Interp.pow2Out(dst / realRange)
                Lines.stroke(waveWidth, R.C.BrainWave)
                Draw.alpha(alpha)
                Lines.circle(x, y, wave.range)
            }
        }

        open fun enemyNearby(): Boolean {
            for (unit in Groups.unit) {
                if (unit.team != team &&
                    !unit.dead &&
                    unit.dst(this) <= range
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
        override fun unit(): Unit {
            //make sure stats are correct
            unit.tile(this)
            unit.team(team)
            return (unit as Unit)
        }
    }
}