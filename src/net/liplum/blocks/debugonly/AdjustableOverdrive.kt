package net.liplum.blocks.debugonly

import arc.Core
import arc.graphics.Color
import arc.math.Mathf
import arc.scene.ui.Slider
import arc.scene.ui.layout.Table
import arc.util.io.Reads
import arc.util.io.Writes
import mindustry.ui.Bar
import mindustry.world.blocks.defense.OverdriveProjector
import net.liplum.utils.*
import kotlin.math.abs

open class AdjustableOverdrive(name: String) : OverdriveProjector(name) {
    @JvmField var maxBoost = 10f
    @JvmField var minBoost = 0.1f
    lateinit var adjustDomainFunc: FUNC
    @JvmField var adjustBase = 2f
    @JvmField var maxGear = 10

    init {
        hasBoost = false
        baseColor = Color.red
        configurable = true
        saveConfig = true
        config(Integer::class.java) { b: AOBuild, i ->
            b.setGear(i.toInt())
        }
        configClear<AOBuild> {
            it.setGear(0)
        }
    }

    override fun setBars() {
        super.setBars()
        bars.add<AOBuild>(
            "boost"
        ) {
            Bar(
                { Core.bundle.format("bar.boost", it.realBoost().percentI) },
                { baseColor },
                { it.realBoost() / maxBoost / 2f }
            )
        }
    }

    override fun init() {
        super.init()
        adjustBase = abs(adjustBase)
        if (adjustBase.isZero()) {
            adjustBase = 1f
        }
        val (func, rfunc) = ExpLogGen(adjustBase)
        adjustDomainFunc = shrink(func, rfunc, minBoost, maxBoost)
    }

    open inner class AOBuild : OverdriveBuild() {
        var curBoost = 0f
        var curGear = 0
        override fun realBoost(): Float = curBoost

        open fun setGear(gear: Int) {
            curGear = abs(gear)
            curBoost = adjustDomainFunc(curGear.toFloat() / maxGear)
        }

        override fun buildConfiguration(table: Table) {
            table.add(Slider(0f, maxGear.toFloat(), 1f, false).apply {
                value = curGear.toFloat()
                moved { configure(Mathf.round(it)) }
            })
        }

        override fun read(read: Reads, revision: Byte) {
            super.read(read, revision)
            curGear = read.b().toInt()
            setGear(curGear)
        }

        override fun write(write: Writes) {
            super.write(write)
            write.b(curGear)
        }
    }
}