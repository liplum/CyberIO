package net.liplum.lib.arc

import arc.func.Cons
import arc.func.Prov
import arc.math.Interp

operator fun <T> Cons<T>.invoke(t: T) {
    this.get(t)
}

operator fun <T> Prov<T>.invoke(): T =
    this.get()

operator fun Interp.invoke(x: Float): Float =
    this.apply(x)
