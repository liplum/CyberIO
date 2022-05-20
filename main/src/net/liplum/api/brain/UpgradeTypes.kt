package net.liplum.api.brain

import arc.util.Time
import mindustry.Vars
import mindustry.world.meta.StatUnit
import net.liplum.utils.percent
import net.liplum.utils.value
import kotlin.math.absoluteValue

typealias UT = UpgradeType

@JvmInline
value class UpgradeType(val type: Int) {
    companion object {
        val Damage = UpgradeType(0)
        val Range = UpgradeType(1)
        val WaveSpeed = UpgradeType(2)
        val WaveWidth = UpgradeType(3)
        val ReloadTime = UpgradeType(4)
        /**
         * To prevent radiating too many waves, it doesn't mean the absolute value will be used.
         */
        val MaxBrainWaveNum = UpgradeType(5)
        val ControlLine = UpgradeType(6)
        val ForceFieldMax = UpgradeType(7)
        val ForceFieldRegen = UpgradeType(8)
        val ForceFieldRadius = UpgradeType(9)
        val ForceFieldRestoreTime = UpgradeType(10)
        val PowerUse = UpgradeType(11)
        val I18ns: Array<UpgradeI18n> = arrayOf(
            UpgradeI18n("damage", {
                it.value(1)
            }, {
                it.percent(1)
            }),
            UpgradeI18n("range", {
                "${(it / Vars.tilesize).value(0)} ${StatUnit.blocks.localized()}"
            }, {
                "${it.percent(0)} ${StatUnit.blocks.localized()}"
            }),
            UpgradeI18n("wave-speed", {
                it.value(2)
            }, {
                it.percent(2)
            }),
            UpgradeI18n("wave-width", {
                it.value(2)
            }, {
                it.percent(2)
            }),
            UpgradeI18n("reload-time", {
                "${(it / 60f).value(1)} ${StatUnit.seconds.localized()}"
            }, {
                it.percent(1)
            }),
            UpgradeI18n("max-brain-wave-num", {
                if (it == it.toInt().toFloat()) it.toInt().value() else it.value(1)
            }, {
                it.value(1)
            }),
            UpgradeI18n("control-line", {
                "|${it.percent(2)}|"
            }, {
                it.percent(2)
            }),
            UpgradeI18n("force-field-max", {
                it.value(0)
            }, {
                it.percent(0)
            }),
            UpgradeI18n("force-field-regen", {
                it.value(1)
            }, {
                it.percent(1)
            }),
            UpgradeI18n("force-field-radius", {
                "${(it / Vars.tilesize).value(1)} ${StatUnit.blocks.localized()}"
            }, {
                "${it.percent(1)} ${StatUnit.blocks.localized()}"
            }),
            UpgradeI18n("force-field-restore-time", {
                "${(it / 60f).value(1)} ${StatUnit.seconds.localized()}"
            }, {
                it.percent(1)
            }),
            UpgradeI18n("power-use", {
                "${(it * 60f).value(0)} ${StatUnit.powerSecond.localized()}"
            }, {
                it.percent(0)
            }),
        )
    }
}

class UpgradeI18n(
    val name: String, val delta: (Float) -> String, val percent: (Float) -> String,
)

data class Upgrade(val type: UpgradeType, val isDelta: Boolean, val value: Float)
data class UpgradeEntry(var value: Float = 0f)

class SpeedScale {
    var scale: Float = 1f
    var duration: Float = 0f
    fun reset() {
        scale = 1f
        duration = 0f
    }

    val value: Float
        get() = if (duration <= 0f) 1f else scale

    fun update(delta: Float = Time.delta) {
        duration -= delta
    }

    fun applySpeedUp(scale: Float, duration: Float = 60f) {
        this.scale = scale.coerceAtLeast(1f)
        this.duration = duration.absoluteValue
    }

    fun applySlowDown(scale: Float, duration: Float = 60f) {
        this.scale = scale.coerceIn(0f, 1f)
        this.duration = duration.absoluteValue
    }

    operator fun plusAssign(scale: Float) {
        applySpeedUp(scale)
    }

    operator fun minusAssign(scale: Float) {
        applySlowDown(1f - scale)
    }
}
