@file:JvmName("ArcH")

package net.liplum.lib.utils

/**
 * In ArcH.kt, you can only import any class from [arc.*]
 */
import arc.audio.Sound
import arc.func.Cons
import arc.func.Prov
import arc.graphics.Color
import arc.math.Interp
import arc.math.Mathf
import arc.struct.*
import net.liplum.lib.math.lerp

fun <T> Seq<T>.removeT(element: T) {
    this.remove(element)
}

val EmptySounds = emptyArray<Sound>()
fun <T> emptySeq(): Seq<T> = ArcU.emptySeq()
fun <T> emptyOrderedSet(): OrderedSet<T> = ArcU.emptySet()
fun <TK, TV> emptyOrderedMap(): OrderedMap<TK, TV> = ArcU.emptyMap()
val Float.isZero: Boolean
    get() = Mathf.zero(this)
val Double.isZero: Boolean
    get() = Mathf.zero(this)

operator fun <K, V> ObjectMap<K, V>.set(key: K, value: V) {
    this.put(key, value)
}

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

private val hsvTemp1 = FloatArray(3)
private val hsvTemp2 = FloatArray(3)
/**
 * @return self
 */
fun Color.hsvLerp(target: Color, progress: Float) = this.apply {
    val hsvA = this.toHsv(hsvTemp1)
    val hsvB = target.toHsv(hsvTemp2)
    hsvA.lerp(hsvB, progress)
    this.fromHsv(hsvA)
}
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

fun String.tinted(color: Color) =
    "[#${color}]$this[]"

fun <T> Seq<T>.isNotEmpty() = !isEmpty
fun <T> Collection<T>.equalsNoOrder(other: Seq<T>): Boolean =
    if (this.size == other.size)
        if (this.isEmpty()) true
        else this.containsAll(other)
    else false

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