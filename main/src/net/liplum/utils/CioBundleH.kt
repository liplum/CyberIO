package net.liplum.utils

import net.liplum.R
import plumy.dsl.bundle
import net.liplum.common.util.format
import kotlin.math.absoluteValue

fun Boolean.yesNo(): String =
    if (this) "yes".bundle
    else "no".bundle
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
fun Float.percent(digit: Int = 2, sign: Boolean = true) =
    if (!sign) {
        (this * 100f).absoluteValue.format(digit)
    } else if (this >= 0f) {
        "+${(this * 100f).format(digit)}%"
    } else {
        "-${(this * 100f).absoluteValue.format(digit)}%"
    }

fun Int.percent(sign: Boolean = true) =
    if (!sign) {
        "$absoluteValue"
    } else if (this >= 0) {
        "+$this%"
    } else {
        "+${absoluteValue}%"
    }
@JvmOverloads
fun Float.value(digit: Int = 2, sign: Boolean = true) =
    if (!sign) {
        absoluteValue.format(digit)
    } else if (this >= 0f) {
        "+${format(digit)}"
    } else {
        "-${absoluteValue.format(digit)}"
    }

fun Int.value(sign: Boolean = true) =
    if (!sign) {
        "$absoluteValue"
    } else if (this >= 0) {
        "+$this"
    } else {
        "-$absoluteValue"
    }
