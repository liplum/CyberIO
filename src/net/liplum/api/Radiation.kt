package net.liplum.api

import arc.util.io.Reads
import arc.util.io.Writes
import net.liplum.persistance.IRWable
import java.util.*

data class Radiation(var range: Float = 0f) : IRWable {
    override fun read(reader: Reads) {
        range = reader.f()
    }

    override fun write(writer: Writes) {
        writer.f(range)
    }
}

class Radiations(val size: Int = 1) : Iterable<Radiation> {
    val list: Array<Radiation> = Array(size) { Radiation() }

    constructor(size: Int = 1, init: (Int, Radiation) -> Unit) : this(size) {
        for ((i, r) in list.withIndex()) {
            init(i, r)
        }
    }

    override fun iterator() = list.iterator()
    fun read(read: Reads) {
        for (i in list.indices) {
            list[i].read(read)
        }
    }

    fun write(write: Writes) {
        for (r in list) {
            r.write(write)
        }
    }
}

class RadiationQueue(val size: Int = 1) : Iterable<Radiation>, IRWable {
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

    override fun read(reader: Reads) {
        val size = list.size
        val targetLen = reader.i()
        if (size == targetLen) {
            for (r in list)
                r.read(reader)
        } else if (size < targetLen) {
            val rest = targetLen - size
            for (r in list)
                r.read(reader)
            for (i in 0 until rest)
                list.addLast(Radiation().apply {
                    read(reader)
                })
        } else { // size > targetLen
            val over = size - targetLen
            var i = 0
            for (r in list) {
                if (i >= targetLen)
                    break
                r.read(reader)
                i++
            }
            for (j in 0 until over)
                list.removeLast()
        }
    }

    override fun write(writer: Writes) {
        writer.i(list.size)
        for (r in list)
            r.write(writer)
    }
}