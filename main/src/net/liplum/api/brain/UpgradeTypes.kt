package net.liplum.api.brain

import arc.scene.ui.layout.Cell
import arc.scene.ui.layout.Table
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
            UpgradeI18n(
                "damage",
                basic = {
                    t.add(it.value(1, sign = false)).unify()
                },
                delta = {
                    t.add(it.value.value(1)).unify()
                },
                percent = {
                    t.add(it.value.percent(1)).unify()
                },
            ),
            UpgradeI18n("range",
                basic = {
                    t.add("${(it / Vars.tilesize).value(0, sign = false)} ${StatUnit.blocks.localized()}").unify()
                },
                delta = {
                    t.add("${(it.value / Vars.tilesize).value(0)} ${StatUnit.blocks.localized()}").unify()
                },
                percent = {
                    t.add("${it.value.percent(0)} ${StatUnit.blocks.localized()}").unify()
                }),
            UpgradeI18n("wave-speed",
                basic = {
                    t.add(it.value(2, sign = false)).unify()
                }, percent = {
                    t.add(it.value.percent(2)).unify()
                },
                delta = {
                    t.add(it.value.value(2)).unify()
                }),
            UpgradeI18n("wave-width",
                basic = {
                    t.add(it.value(2, sign = false)).unify()
                }, percent = {
                    t.add(it.value.percent(2)).unify()
                }, delta = {
                    t.add(it.value.value(2)).unify()
                }),
            UpgradeI18n("reload-time",
                basic = {
                    t.add("${(it / 60f).value(1, sign = false)} ${StatUnit.seconds.localized()}").unify()
                }, percent = {
                    t.add(it.value.percent(1)).unify()
                }, delta = {
                    t.add("${(it.value / 60f).value(1)} ${StatUnit.seconds.localized()}").unify()
                }),
            UpgradeI18n("max-brain-wave-num",
                basic = {
                    t.add(
                        if (it == it.toInt().toFloat())
                            it.toInt().value(sign = false) else it.value(1, sign = false)
                    ).unify()
                }, percent = {
                    t.add(it.value.value(1)).unify()
                }, delta = {
                    it.value.run {
                        t.add(
                            if (this == this.toInt().toFloat()) this.toInt().value(sign = false)
                            else this.value(1, sign = false)
                        ).unify()
                    }
                }),
            UpgradeI18n("control-line",
                basic = {
                    t.add("|${it.percent(2, sign = false)}|").unify()
                }, percent = {
                    t.add(it.value.percent(2)).unify()
                }, delta = {
                    t.add("|${it.value.percent(2)}|").unify()
                }),
            UpgradeI18n("force-field-max",
                basic = {
                    t.add(it.value(0, sign = false)).unify()
                }, percent = {
                    t.add(it.value.percent(0)).unify()
                }, delta = {
                    t.add(it.value.value(0)).unify()
                }),
            UpgradeI18n("force-field-regen", basic = {
                t.add(it.value(1, sign = false)).unify()
            }, percent = {
                t.add(it.value.percent(1)).unify()
            }, delta = {
                t.add(it.value.value(1)).unify()
            }),
            UpgradeI18n("force-field-radius",
                basic = {
                    t.add("${(it / Vars.tilesize).value(1, sign = false)} ${StatUnit.blocks.localized()}").unify()
                }, percent = {
                    "${it.value.percent(1)} ${StatUnit.blocks.localized()}"
                }, delta = {
                    t.add("${(it.value / Vars.tilesize).value(1)} ${StatUnit.blocks.localized()}").unify()
                }),
            UpgradeI18n("force-field-restore-time",
                basic = {
                    t.add("${(it / 60f).value(1, sign = false)} ${StatUnit.seconds.localized()}").unify()
                }, percent = {
                    t.add(it.value.percent(1)).unify()
                }, delta = {
                    t.add("${(it.value / 60f).value(1)} ${StatUnit.seconds.localized()}").unify()
                }),
            UpgradeI18n("power-use",
                basic = {
                    t.add("${(it * 60f).value(0, sign = false)} ${StatUnit.powerSecond.localized()}").unify()
                }, percent = {
                    t.add(it.value.percent(0)).unify()
                }, delta = {
                    t.add("${(it.value * 60f).value(0)} ${StatUnit.powerSecond.localized()}").unify()
                }),
        )
    }
}
@JvmInline
value class UpgradeStatTable(
    val t: Table,
) {
    fun Cell<*>.unify() = apply {
        padRight(2f).right()
    }
}

class UpgradeI18n(
    val name: String,
    val basic: UpgradeStatTable.(Float) -> Unit,
    val delta: UpgradeStatTable.(Upgrade) -> Unit,
    val percent: UpgradeStatTable.(Upgrade) -> Unit,
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
