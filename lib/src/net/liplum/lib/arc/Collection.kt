package net.liplum.lib.arc

import arc.math.Mathf
import arc.struct.*

/**
 * It calls the [IntSet.iterator] function. Note this can't work in multi-thread or nested calling.
 */
inline fun IntSet.forEach(func: (Int) -> Unit) {
    val it = this.iterator()
    while (it.hasNext) {
        func(it.next())
    }
}

inline fun IntSeq.forEach(func: (Int) -> Unit) {
    for (i in 0 until size) {
        func(this[i])
    }
}
/**
 * Only used in single thread.
 */
val tempIntSeq = IntSeq(64)
/**
 * Only used in single thread.
 * It uses [tempIntSeq] as temporary [IntSeq]
 */
inline fun IntSeq.snapshotForEach(func: (Int) -> Unit) {
    snapShot(tempIntSeq).run {
        for (i in 0 until size) {
            func(this[i])
        }
    }
}
/**
 * @return [temp]
 */
fun IntSeq.snapShot(temp: IntSeq): IntSeq {
    temp.copyFieldsFrom(this)
    return temp
}

fun IntSeq.copyFieldsFrom(other: IntSeq) {
    this.ordered = other.ordered
    if (size < other.size) {
        items = IntArray(other.size)
    }
    size = other.size
    System.arraycopy(other.items, 0, items, 0, size)
}
/**
 * It calls the [IntSet.iterator] function. Note this can't work in multi-thread or nested calling.
 * @param func (Index,Element)
 */
inline fun IntSet.forEachIndexed(func: (Int, Int) -> Unit) {
    val it = this.iterator()
    var i = 0
    while (it.hasNext) {
        func(i, it.next())
        i++
    }
}
/**
 * @param func (Index,Element)
 */
inline fun IntSeq.forEachIndexed(func: (Int, Int) -> Unit) {
    for (i in 0 until size) {
        func(i, this[i])
    }
}
/**
 * It calls the [IntSet.iterator] function. Note this can't work in multi-thread or nested calling.
 */
inline fun IntSet.removeAll(predicate: (Int) -> Boolean) {
    val it = this.iterator()
    while (it.hasNext) {
        if (predicate(it.next())) {
            it.remove()
        }
    }
}

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

fun <T> Collection<T>.equalsNoOrder(other: Seq<T>): Boolean =
    if (this.size == other.size)
        if (this.isEmpty()) true
        else other.containsAll(this)
    else false

fun <T> Seq<T>.isNotEmpty() = !isEmpty
fun <T> Seq<T>.containsAll(other: Collection<T>): Boolean {
    for (e in other)
        if (!this.contains(e))
            return false
    return true
}

fun <T> Seq<T>.equalsNoOrder(other: Collection<T>): Boolean =
    if (this.size == other.size)
        if (this.isEmpty) true
        else this.containsAll(other)
    else false

fun <T> Seq<T>.set(other: Iterable<T>) = apply {
    clear()
    for (e in other)
        this.add(e)
}
fun <T> Seq<T>.set(other: Collection<T>) = apply {
    clear()
    for (e in other)
        this.add(e)
}
/**
 * Using index instead of iterator.
 */
inline fun <T> Seq<T>.forLoop(func: (T) -> Unit) = apply {
    for (i in 0 until this.size) {
        func(this[i])
    }
}

fun <T> Seq<T>.shrinkTo(targetSize: Int) = apply {
    if (size > targetSize) {
        removeLastRange(size - targetSize)
    }
}
/**
 * It doesn't break the order if this [Seq] ordered.
 */
fun <T> Seq<T>.removeLast() = apply {
    check(size != 0) { "Array is empty." }
    this.items[size - 1] = null
    --size
}
/**
 * It doesn't break the order if this [Seq] ordered.
 */
fun <T> Seq<T>.removeLastRange(length: Int) = apply {
    if (length > size) throw IndexOutOfBoundsException("length can't be > size: $length > $size")
    if (length == size) this.clear()
    else {
        for (i in 0 until size - length) {
            this.items[size - 1] = null
            --size
        }
    }
}

operator fun <T> IntMap<T>.set(key: Int, value: T) {
    put(key, value)
}

fun <T> Array<T>.random(): T =
    this[Mathf.random(size - 1).coerceAtLeast(0)]

fun <T> Seq<T>.removeT(element: T) {
    this.remove(element)
}

operator fun <K, V> ObjectMap<K, V>.set(key: K, value: V) {
    this.put(key, value)
}
