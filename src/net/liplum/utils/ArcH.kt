package net.liplum.utils

import arc.func.Cons
import arc.func.Prov
import arc.math.Interp
import arc.math.Mathf
import arc.struct.OrderedMap
import arc.struct.OrderedSet
import arc.struct.Seq

fun <T> Seq<T>.removeT(element: T) {
    this.remove(element)
}

fun <T> emptySeq(): Seq<T> = ArcU.emptySeq()
fun <T> emptyOrderedSet(): OrderedSet<T> = ArcU.emptySet()
fun <TK, TV> emptyOrderedMap(): OrderedMap<TK, TV> = ArcU.emptyMap()
val Float.isZero: Boolean
    get() = Mathf.zero(this)

operator fun <T> Cons<T>.invoke(t: T) {
    this.get(t)
}

operator fun <T> Prov<T>.invoke(): T =
    this.get()

operator fun Interp.invoke(x: Float): Float =
    this.apply(x)

inline fun <T> Seq<T>.insertAfter(e: T, whenTrue: (T) -> Boolean) {
    for ((i, v) in this.withIndex()) {
        if (whenTrue(v)) {
            this.insert(i, e)
            return
        }
    }
}

inline fun <T> Seq<T>.insertBefore(e: T, whenTrue: (T) -> Boolean) {
    for ((i, v) in this.withIndex()) {
        if (whenTrue(v)) {
            this.insert(i - 1, e)
            return
        }
    }
}

fun <T> Array<T>.random(): T =
    this[Mathf.random(size - 1).coerceAtLeast(0)]