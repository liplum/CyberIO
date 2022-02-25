package net.liplum.utils

fun Double.format(digits: Int) = "%.${digits}f".format(this)
fun Float.format(digits: Int) = "%.${digits}f".format(this)
infix fun Float.coIn(abs: Float) = this.coerceIn(-abs, abs)
infix fun Double.coIn(abs: Double) = this.coerceIn(-abs, abs)