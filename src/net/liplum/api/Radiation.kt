package net.liplum.api

import java.util.*

data class Radiation(var range: Float = 0f)
class Radiations(val size: Int = 1) : Iterable<Radiation> {
    val list: Array<Radiation> = Array(size) { Radiation() }

    constructor(size: Int = 1, init: (Int, Radiation) -> Unit) : this(size) {
        for ((i, r) in list.withIndex()) {
            init(i, r)
        }
    }

    override fun iterator() = list.iterator()
}

class RadiationQueue(val size: Int = 1) : Iterable<Radiation> {
    val list: LinkedList<Radiation> = LinkedList()
    val canAdd: Boolean
        get() = list.size < size

    override fun iterator() = list.iterator()
    /**
     * Remove the first-added
     */
    fun poll(): Radiation? {
        return list.pollFirst()
    }
    /**
     * Remove the first-added if it meets the [predicate].
     */
    inline fun pollWhen(predicate: (Radiation) -> Boolean): Radiation? {
        if (list.size > 0 && predicate(list.first)) {
            return list.pollFirst()
        }
        return null
    }
    /**
     * Remove the last-added.
     */
    fun pop(): Radiation? {
        return list.pollLast()
    }
    /**
     * Remove the last-added if it meets the [predicate].
     */
    inline fun popWhen(predicate: (Radiation) -> Boolean): Radiation? {
        if (list.size > 0 && predicate(list.last)) {
            return list.pollLast()
        }
        return null
    }
    /**
     * Add a new radiation at end.
     */
    fun append(radiation: Radiation) {
        return list.addLast(radiation)
    }
    /**
     * Add a new radiation at start.
     */
    fun push(radiation: Radiation) {
        list.addFirst(radiation)
    }
}