package net.liplum.lib.arc

typealias Tick = Float
typealias Second = Float
typealias Minute = Float

val Tick.second
    get() = this * 60f
val Tick.minute
    get() = this * 60f * 60f
val Int.second
    get() = this * 60f
val Int.minute
    get() = this * 60f * 60f
val Double.second: Float
    get() = (this * 60f).toFloat()
val Double.minute: Float
    get() = (this * 60f * 60f).toFloat()