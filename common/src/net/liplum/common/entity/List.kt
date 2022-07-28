package net.liplum.common.entity

import arc.util.io.Reads
import arc.util.io.Writes
import net.liplum.common.persistence.CacheReaderSpec
import net.liplum.common.persistence.CacheWriter
import net.liplum.common.persistence.IRWableX
import net.liplum.common.util.ArrayList
import java.io.DataInputStream
import java.util.*

class FixedList<T : IRWableX>(
    @JvmField
    val size: Int = 1,
    @JvmField
    val creator: () -> T,
) : Iterable<T>, IRWableX {
    val list: ArrayList<T> = ArrayList(size) {
        creator()
    }

    constructor(
        size: Int = 1,
        creator: () -> T,
        init: (Int, T) -> Unit,
    ) : this(size, creator) {
        for ((i, r) in list.withIndex()) {
            init(i, r)
        }
    }

    override fun iterator() = list.iterator()
    override fun read(reader: Reads) {
        for (i in list.indices) {
            list[i].read(reader)
        }
    }

    override fun read(reader: DataInputStream) {
        for (i in list.indices) {
            list[i].read(reader)
        }
    }

    override fun write(writer: Writes) {
        for (r in list) {
            r.write(writer)
        }
    }

    override fun write(writer: CacheWriter) {
        for (r in list) {
            r.write(writer)
        }
    }
}

class Queue<T : IRWableX>(
    @JvmField
    var maxSize: () -> Int = { 1 },
    @JvmField
    val creator: () -> T,
) : Iterable<T>, IRWableX {
    constructor(maxSize: Int, creator: () -> T) : this({ maxSize }, creator)
    @JvmField
    val list: LinkedList<T> = LinkedList()
    val size: Int
        get() = list.size
    val canAdd: Boolean
        get() = list.size < maxSize()

    override fun iterator() = list.iterator()
    fun reverseIterator(): MutableIterator<T> = list.descendingIterator()
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
     * Add a new element at end.
     */
    fun add(e: T) {
        if (canAdd)
            list.addLast(e)
    }
    /**
     * Add a new element at end.
     */
    fun append(e: T) {
        if (canAdd)
            list.addLast(e)
    }
    /**
     * Add a new element at start.
     */
    fun push(e: T) {
        if (canAdd)
            list.addFirst(e)
    }

    inline fun removeAll(predicate: (T) -> Boolean) {
        val it = iterator()
        while (it.hasNext()) {
            if (predicate(it.next())) {
                it.remove()
            }
        }
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
            // Append more until the same size
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
                // Desert rest
                r.read(reader)
                i++
            }
            for (j in 0 until over)
                list.removeLast()
        }
    }

    override fun read(reader: DataInputStream) = CacheReaderSpec(reader).run {
        val size = list.size
        val targetLen = i()
        if (size == targetLen) {
            for (r in list)
                r.read(reader)
        } else if (size < targetLen) {
            val rest = targetLen - size
            for (r in list)
                r.read(reader)
            // Append more until the same size
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
                // Desert rest
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

    override fun write(writer: CacheWriter) {
        writer.i(list.size)
        for (r in list)
            r.write(writer)
    }
}
