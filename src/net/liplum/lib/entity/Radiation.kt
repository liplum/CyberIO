package net.liplum.lib.entity

import arc.util.io.Reads
import arc.util.io.Writes
import net.liplum.persistance.IRWable
import net.liplum.utils.ArrayList
import java.util.*

open class Radiation(var range: Float = 0f) : IRWable {
    override fun read(reader: Reads) {
        range = reader.f()
    }

    override fun write(writer: Writes) {
        writer.f(range)
    }
}

class PosRadiation(
    range: Float = 0f,
    var x: Float = 0f,
    var y: Float = 0f
) : Radiation(range) {
    override fun read(reader: Reads) {
        super.read(reader)
        x = reader.f()
        y = reader.f()
    }

    override fun write(writer: Writes) {
        super.write(writer)
        writer.f(x)
        writer.f(y)
    }
}

class Radiations<T : Radiation>(
    val size: Int = 1,
    val creator: () -> T
) : Iterable<T> {
    val list: ArrayList<T> = ArrayList(size) {
        creator()
    }

    constructor(
        size: Int = 1,
        creator: () -> T,
        init: (Int, T) -> Unit
    ) : this(size, creator) {
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

fun RadiationArray(size: Int = 1) =
    Radiations(size, ::Radiation)

fun PosRadiationArray(size: Int = 1) =
    Radiations(size, ::PosRadiation)

fun RadiationArray(size: Int = 1, init: (Int, Radiation) -> Unit) =
    Radiations(size, ::Radiation, init)

fun PosRadiationArray(size: Int = 1, init: (Int, PosRadiation) -> Unit) =
    Radiations(size, ::PosRadiation, init)

class RadiationQueue<T : Radiation>(var size: Int = 1, val creator: () -> T) : Iterable<T>, IRWable {
    val list: LinkedList<T> = LinkedList()
    val canAdd: Boolean
        get() = list.size < size

    override fun iterator() = list.iterator()
    /**
     * Remove the first-added
     */
    fun poll(): T? {
        return list.pollFirst()
    }

    inline fun RemoveAllWhen(crossinline predicate: (T) -> Boolean): Boolean {
        return list.removeIf { predicate(it) }
    }
    /**
     * Remove the first-added if it meets the [predicate].
     */
    inline fun pollWhen(predicate: (T) -> Boolean): T? {
        if (list.size > 0 && predicate(list.first)) {
            return list.pollFirst()
        }
        return null
    }
    /**
     * Remove the last-added.
     */
    fun pop(): T? {
        return list.pollLast()
    }
    /**
     * Remove the last-added if it meets the [predicate].
     */
    inline fun popWhen(predicate: (T) -> Boolean): T? {
        if (list.size > 0 && predicate(list.last)) {
            return list.pollLast()
        }
        return null
    }
    /**
     * Add a new radiation at end.
     */
    fun append(radiation: T) {
        return list.addLast(radiation)
    }
    /**
     * Add a new radiation at start.
     */
    fun push(radiation: T) {
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
                list.addLast(creator().apply {
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

fun RadiationQueue(size: Int = 1) =
    RadiationQueue(size, ::Radiation)

fun PosRadiationQueue(size: Int = 1) =
    RadiationQueue(size, ::PosRadiation)
