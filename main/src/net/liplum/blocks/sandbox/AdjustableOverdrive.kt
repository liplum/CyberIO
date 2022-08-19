package net.liplum.blocks.sandbox

import arc.Core
import arc.func.Prov
import arc.graphics.Color
import arc.math.Mathf
import arc.scene.ui.Slider
import arc.scene.ui.layout.Table
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.graphics.Drawf
import mindustry.world.blocks.defense.OverdriveProjector
import mindustry.world.meta.Stat
import net.liplum.R
import plumy.dsl.bundle
import net.liplum.common.util.percentI
import net.liplum.math.shrink
import plumy.core.Serialized
import plumy.core.math.ExpLogGen
import plumy.core.math.FUNC
import plumy.core.math.isZero
import plumy.dsl.AddBar
import plumy.dsl.config
import plumy.dsl.configNull
import kotlin.math.abs

open class AdjustableOverdrive(name: String) : OverdriveProjector(name) {
    @JvmField var maxBoost = 10f
    @JvmField var minBoost = 0.1f
    lateinit var adjustDomainFunc: FUNC
    @JvmField var adjustBase = 2f
    @JvmField var maxGear = 10

    init {
        buildType = Prov { AOBuild() }
        hasBoost = false
        baseColor = Color.red
        configurable = true
        saveConfig = true
        updateInUnits = true
        alwaysUpdateInUnits = true
        // For connect
        config<AOBuild, Int> {
            setGear(it)
        }
        configNull<AOBuild> {
            setGear(0)
        }
    }

    override fun setBars() {
        super.setBars()
        AddBar<AOBuild>("boost",
            { Core.bundle.format("bar.boost", realBoost().percentI) },
            { baseColor },
            { realBoost() / maxBoost }
        )
    }

    override fun setStats() {
        super.setStats()
        stats.remove(Stat.speedIncrease)
        stats.add(Stat.speedIncrease) {
            it.add(
                R.Bundle.Gen("speed-increase.range").bundle(
                    minBoost * 100f, maxBoost * 100f
                )
            )
        }
    }

    override fun init() {
        super.init()
        adjustBase = abs(adjustBase)
        if (adjustBase.isZero) {
            adjustBase = 1f
        }
        val (func, rfunc) = ExpLogGen(adjustBase)
        adjustDomainFunc = shrink(func, rfunc, minBoost, maxBoost)
    }

    open inner class AOBuild : OverdriveBuild() {
        var curBoost = 0f
        @Serialized
        var curGear = 0
        override fun realBoost(): Float = curBoost
        open fun setGear(gear: Int) {
            curGear = abs(gear)
            curBoost = adjustDomainFunc(curGear.toFloat() / maxGear)
        }

        val realRange: Float
            get() = range + phaseHeat * phaseRangeBoost

        override fun drawConfigure() {
            super.drawConfigure()
            Drawf.dashCircle(x, y, realRange, baseColor)
        }

        override fun buildConfiguration(table: Table) {
            table.add(Slider(0f, maxGear.toFloat(), 1f, false).apply {
                value = curGear.toFloat()
                moved { configure(Mathf.round(it)) }
            }).width(180f).growX()
        }

        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            curGear = read.b().toInt()
        }

        override fun afterRead() {
            super.afterRead()
            setGear(curGear)
        }

        override fun write(write: Writes) {
            super.write(write)
            write.b(curGear)
        }
    }
}