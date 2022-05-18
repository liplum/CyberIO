package net.liplum.utils

import net.liplum.R
import net.liplum.lib.utils.bundle
import net.liplum.lib.utils.format
import kotlin.math.absoluteValue

fun Boolean.yesNo(): String =
    if (this)
        R.Ctrl.Yes.bundle
    else
        R.Ctrl.No.bundle
@JvmOverloads
fun Int.time(coerceMinute: Boolean = false): String {
    val min = this / 60
    val sec = this % 60
    return if (min > 0 || coerceMinute)
        "$min ${R.Bundle.CostMinute.bundle} $sec ${R.Bundle.CostSecond.bundle}"
    else
        "$sec ${R.Bundle.CostSecond.bundle}"
}
@JvmOverloads
fun Float.percent(digit: Int = 2) =
    if (this >= 0f) {
        R.Bundle.PercentPlus.bundle((this * 100f).absoluteValue.format(digit))
    } else {
        R.Bundle.PercentMinus.bundle((this * 100f).absoluteValue.format(digit))
    }

fun Int.percent() =
    if (this >= 0) {
        R.Bundle.PercentPlus.bundle(this.absoluteValue)
    } else {
        R.Bundle.PercentMinus.bundle(this.absoluteValue)
    }
@JvmOverloads
fun Float.value(digit: Int = 2) =
    if (this >= 0f) {
        R.Bundle.ValuePlus.bundle(this.absoluteValue.format(digit))
    } else {
        R.Bundle.ValueMinus.bundle(this.absoluteValue.format(digit))
    }

fun Int.value() =
    if (this >= 0) {
        R.Bundle.ValuePlus.bundle(this.absoluteValue)
    } else {
        R.Bundle.ValueMinus.bundle(this.absoluteValue)
    }