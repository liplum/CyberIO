@file:JvmName("KotlinH")

package net.liplum.common.util

import plumy.core.Out

typealias Index = Int

fun Double.format(digits: Int) = "%.${digits}f".format(this)
fun Float.format(digits: Int) = "%.${digits}f".format(this)
infix fun Float.coIn(abs: Float) = this.coerceIn(-abs, abs)
infix fun Double.coIn(abs: Double) = this.coerceIn(-abs, abs)
val Double.intStr: String
    get() = this.toInt().toString()
val Float.intStr: String
    get() = this.toInt().toString()
val Double.percentI: Int
    get() = (this * 100).toInt()
val Float.percentI: Int
    get() = (this * 100).toInt()

inline fun <T> ArrayList(len: Int, gen: (Int) -> T) =
    ArrayList<T>(len).apply {
        for (i in 0 until len) {
            add(gen(i))
        }
    }

infix fun Int.between(end: Int): IntRange {
    return IntRange(this + 1, end - 1)
}

val EmptyArrays = HashMap<Class<*>, Array<*>>()
@Suppress("UNCHECKED_CAST")
inline fun <reified T> Class<T>.EmptyArray(): Array<T> =
    EmptyArrays.getOrPut(this) { emptyArray<T>() } as Array<T>

val Boolean.Int: Int
    get() = if (this) 1 else 0
val Boolean.Float: Float
    get() = if (this) 1f else 0f
/**
 * Using Shallow [Array.contentEquals]
 */
fun <T> Array<T>.equalsNoOrder(other: Array<T>): Boolean =
    if (this.size == other.size)
        if (this.isEmpty()) true
        else this.contentEquals(other)
    else false

fun <T> Collection<T>.equalsNoOrder(other: Collection<T>): Boolean =
    if (this.size == other.size)
        if (this.isEmpty()) true
        else this.containsAll(other)
    else false

fun <T> Collection<T>.containsAll(other: Iterable<T>): Boolean {
    for (e in other)
        if (!this.contains(e))
            return false
    return true
}

val Int.isOdd: Boolean
    get() = this % 2 == 1
val Int.isEven: Boolean
    get() = this % 2 == 0

infix fun Any?.NullOr(planB: Any?): Any? =
    this ?: planB

inline infix fun <reified T> T?.Or(other: () -> T?): T? =
    this ?: other()

fun <T> Array<T>.swap(from: Int, to: Int) {
    val temp: T = this[from]
    this[from] = this[to]
    this[to] = temp
}
@Suppress("UNCHECKED_CAST")
inline fun <reified T> newArray(size: Int): Array<T> {
    return arrayOfNulls<T>(size) as Array<T>
}

inline fun <reified T> Array<T>.sortManually(vararg indices: Int): Array<T> {
    assert(indices.size == this.size)
    val res = newArray<T>(this.size)
    for ((i, index) in indices.withIndex()) {
        res[i] = this[index]
    }
    return res
}

fun Boolean.toInt(): Int = if (this) 1 else 0
fun Boolean.toFloat(): Float = if (this) 1f else 0f
fun Boolean.toDouble(): Double = if (this) 1.0 else 0.0
/**
 * Roll an element from a collection.
 * @return the result that doesn't equal to [exception].
 * Otherwise, null will be returned.
 */
fun <T> Collection<T>.randomExcept(
    atLeast: Boolean = false,
    exception: T,
): T? {
    when (size) {
        0 -> return null
        1 -> return if (first() != exception && !atLeast)
            first()
        else null
    }
    while (true) {
        val res = this.random()
        if (res != exception)
            return res
    }
}
/**
 * Roll an element from a collection.
 * @return the result that isn't inclining in this collection.
 * Otherwise, null will be returned.
 */
inline fun <reified T> Collection<T>.randomExcept(
    maxTry: Int = this.size,
    atLeastOne: Boolean = false,
    isInclude: T.() -> Boolean,
): T? {
    when (size) {
        0 -> return null
        1 -> {
            val first = first()
            return if (first.isInclude() && !atLeastOne)
                null
            else
                first
        }
    }
    for (i in 0 until maxTry) {
        val res = this.random()
        if (!res.isInclude())
            return res
    }
    return if (atLeastOne)
        this.random()
    else
        null
}
/**
 * Roll an element from a collection.
 * @return the result that isn't inclining in this collection.
 * Otherwise, null will be returned.
 */
inline fun <reified C, reified T> C.randomExcept(
    maxTry: Int = this.size,
    atLeastOne: Boolean = false,
    random: C.() -> T,
    isInclude: T.() -> Boolean,
): T? where C : Collection<T> {
    when (size) {
        0 -> return null
        1 -> {
            val first = first()
            return if (first.isInclude() && !atLeastOne)
                null
            else
                first
        }
    }
    for (i in 0 until maxTry) {
        val res = this.random()
        if (!res.isInclude())
            return res
    }
    return if (atLeastOne)
        this.random()
    else
        null
}
/**
 * Returns the all elements yielding the largest value of the given function or empty list if there are no elements.
 */
inline fun <reified T, reified R : Comparable<R>> Iterable<T>.allMaxBy(selector: (T) -> R): List<T> {
    val iterator = iterator()
    if (!iterator.hasNext()) return emptyList()
    var maxElem = iterator.next()
    if (!iterator.hasNext()) return listOf(maxElem)
    var maxValue = selector(maxElem)
    val res = ArrayList<T>().apply {
        add(maxElem)
    }
    do {
        val e = iterator.next()
        val v = selector(e)
        if (maxValue < v) {
            maxElem = e
            maxValue = v
            res.clear()
            res.add(e)
        } else if (maxValue == v) {
            res.add(e)
        }
    } while (iterator.hasNext())
    return res
}

inline fun <reified K, reified V, reified R : Comparable<R>> Map<out K, V>.allMaxBy(
    selector: (Map.Entry<K, V>) -> R,
): List<Map.Entry<K, V>> {
    return entries.allMaxBy(selector)
}

val Any.directSuperClass: Class<*>
    get() {
        val clz = javaClass
        return if (clz.isAnonymousClass) clz.superclass else clz
    }

fun <T> Array<T>.progress(progress: Float): T {
    val p = progress.coerceIn(0f, 1f)
    val index = (p * size).toInt().coerceAtMost(size - 1)
    return this[index]
}

inline fun Int.forLoop(func: (Int) -> Unit) {
    for (i in 0 until this)
        func(i)
}

fun <T> Array<T>.rotateInto(@Out out: Array<T>, rotator: Int = 1): Array<T> {
    if (isEmpty()) throw NoSuchElementException("Array is empty.")
    for (i in 0 until this.size) {
        out[(i + rotator) % this.size] = this[i]
    }
    return out
}
/**
 * Rotate current array 1 step.
 * @param forward whether every index will increase 1
 */
fun <T> Array<T>.rotateOnce(forward: Boolean = true): Array<T> {
    if (isEmpty() || size == 1) return this
    if (forward) {
        // the last one will be the first one
        val last = this.last()
        for (i in size - 1 downTo 1) {// stop before the last one
            this[i] = this[i - 1]
        }
        this[0] = last
    } else {
        // the first one will be the last one
        val first = first()
        for (i in 0 until size - 1) {// stop at the last one
            this[i] = this[i + 1]
        }
        this[lastIndex] = first
    }
    return this
}

fun IntArray.copyFrom(other: IntArray) {
    System.arraycopy(other, 0, this, 0, this.size)
}

fun <T> MutableCollection<T>.swap(other: MutableCollection<T>, temp: MutableCollection<T>) {
    temp.clear()
    temp.addAll(this)
    this.clear()
    this.addAll(other)
    other.clear()
    other.addAll(temp)
}

inline fun <T> MutableList<T>.shrinkTo(
    targetSize: Int,
    removing: MutableList<T>.() -> Unit = { this.removeLast() },
) {
    if (size <= targetSize) return
    for (i in 0 until targetSize - size) {
        this.removing()
    }
}

inline fun <T> coherentApply(
    o1: T, o2: T, func: T.() -> Unit,
) {
    o1.func()
    o2.func()
}