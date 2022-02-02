package net.liplum.blocks

import arc.Core
import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Lines
import arc.math.Mathf
import arc.math.geom.Geometry
import arc.util.Time
import arc.util.Tmp
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.Vars
import mindustry.gen.Building
import mindustry.graphics.Drawf
import mindustry.graphics.Pal
import mindustry.logic.Ranged
import mindustry.ui.Bar
import mindustry.world.Block
import mindustry.world.meta.BlockGroup
import mindustry.world.meta.Stat
import mindustry.world.meta.StatUnit

open class UnderdriveProjector(name: String?) : Block(name) {
    val timerUse = timers++
    var reload = 60f
    var range = 80f
    var speedBoost = 1.5f
    var speedBoostPhase = 0.75f
    var useTime = 400f
    var phaseRangeBoost = 20f
    var hasBoost = true
    var baseColor = Color.valueOf("feb380")
    var phaseColor = Color.valueOf("ffd59e")

    init {
        solid = true
        update = true
        group = BlockGroup.projectors
        hasPower = true
        hasItems = true
        canOverdrive = false
    }

    override fun outputsItems(): Boolean {
        return false
    }

    override fun drawPlace(x: Int, y: Int, rotation: Int, valid: Boolean) {
        super.drawPlace(x, y, rotation, valid)
        Drawf.dashCircle(x * Vars.tilesize + offset, y * Vars.tilesize + offset, range, baseColor)
        Vars.indexer.eachBlock(Vars.player.team(), x * Vars.tilesize + offset, y * Vars.tilesize + offset, range,
            { other: Building -> other.block.canOverdrive }
        ) { other: Building? -> Drawf.selected(other, Tmp.c1.set(baseColor).a(Mathf.absin(4f, 1f))) }
    }

    override fun setStats() {
        super.setStats()
        stats.add(Stat.speedIncrease, (100f * speedBoost).toInt().toFloat(), StatUnit.percent)
        stats.add(Stat.range, range / Vars.tilesize, StatUnit.blocks)
        stats.add(Stat.productionTime, useTime / 60f, StatUnit.seconds)
        if (hasBoost) {
            stats.add(Stat.boostEffect, phaseRangeBoost / Vars.tilesize, StatUnit.blocks)
            stats.add(Stat.boostEffect, ((speedBoost + speedBoostPhase) * 100f).toInt().toFloat(), StatUnit.percent)
        }
    }

    override fun setBars() {
        super.setBars()
        bars.add(
            "boost"
        ) { entity: UnderdriveBuild ->
            Bar(
                { Core.bundle.format("bar.boost", (entity.realBoost() * 100).toInt()) },
                { Pal.accent }
            ) { entity.realBoost() / if (hasBoost) speedBoost + speedBoostPhase else speedBoost }
        }
    }

    open inner class UnderdriveBuild : Building(), Ranged {
        var heat = 0f
        var charge = Mathf.random(reload)
        var phaseHeat = 0f
        var smoothEfficiency = 0f
        override fun range(): Float {
            return range
        }

        override fun drawLight() {
            Drawf.light(team, x, y, 50f * smoothEfficiency, baseColor, 0.7f * smoothEfficiency)
        }

        override fun updateTile() {
            smoothEfficiency = Mathf.lerpDelta(smoothEfficiency, efficiency(), 0.08f)
            heat = Mathf.lerpDelta(heat, if (consValid()) 1f else 0f, 0.08f)
            charge += heat * Time.delta
            if (hasBoost) {
                phaseHeat = Mathf.lerpDelta(phaseHeat, Mathf.num(cons.optionalValid()).toFloat(), 0.1f)
            }
            if (charge >= reload) {
                val realRange = range + phaseHeat * phaseRangeBoost
                charge = 0f
                Vars.indexer.eachBlock(this, realRange,
                    { other: Building? -> true }
                ) { other: Building -> other.applyBoost(realBoost(), reload + 1f) }
            }
            if (timer(timerUse, useTime) && efficiency() > 0 && consValid()) {
                consume()
            }
        }

        open fun realBoost(): Float {
            return if (consValid()) (speedBoost + phaseHeat * speedBoostPhase) * efficiency() else 0f
        }

        override fun drawSelect() {
            val realRange = range + phaseHeat * phaseRangeBoost
            Vars.indexer.eachBlock(this, realRange,
                { other: Building -> other.block.canOverdrive }
            ) { other: Building? ->
                Drawf.selected(
                    other,
                    Tmp.c1.set(baseColor).a(Mathf.absin(4f, 1f))
                )
            }
            Drawf.dashCircle(x, y, realRange, baseColor)
        }

        override fun draw() {
            super.draw()
            val f = 1f - Time.time / 100f % 1f
            Draw.color(baseColor, phaseColor, phaseHeat)
            Draw.alpha(heat * Mathf.absin(Time.time, 10f, 1f) * 0.5f)
            Draw.alpha(1f)
            Lines.stroke((2f * f + 0.1f) * heat)
            val r = Math.max(0f, Mathf.clamp(2f - f * 2f) * size * Vars.tilesize / 2f - f - 0.2f)
            val w = Mathf.clamp(0.5f - f) * size * Vars.tilesize
            Lines.beginLine()
            for (i in 0..3) {
                Lines.linePoint(
                    x + Geometry.d4(i).x * r + Geometry.d4(i).y * w,
                    y + Geometry.d4(i).y * r - Geometry.d4(i).x * w
                )
                if (f < 0.5f) Lines.linePoint(
                    x + Geometry.d4(i).x * r - Geometry.d4(i).y * w,
                    y + Geometry.d4(i).y * r + Geometry.d4(i).x * w
                )
            }
            Lines.endLine(true)
            Draw.reset()
        }

        override fun write(write: Writes) {
            super.write(write)
            write.f(heat)
            write.f(phaseHeat)
        }

        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            heat = read.f()
            phaseHeat = read.f()
        }
    }
}
