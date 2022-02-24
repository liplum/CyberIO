package net.liplum.utils

import arc.struct.Seq

fun <T> Seq<T>.removeT(element: T) {
    this.remove(element)
}