package net.liplum.utils

import arc.math.Mathf
import arc.struct.OrderedMap
import arc.struct.OrderedSet
import arc.struct.Seq

fun <T> Seq<T>.removeT(element: T) {
    this.remove(element)
}

fun <T> emptySeq(): Seq<T> = ArcU.emptySeq()
fun <T> emptySet(): OrderedSet<T> = ArcU.emptySet()
fun <TK, TV> emptyMap(): OrderedMap<TK, TV> = ArcU.emptyMap()

fun Float.isZero() = Mathf.zero(this)