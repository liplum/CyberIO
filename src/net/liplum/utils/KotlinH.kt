package net.liplum.utils

fun Double.format(digits: Int) = "%.${digits}f".format(this)
fun Float.format(digits: Int) = "%.${digits}f".format(this)
infix fun Float.coIn(abs: Float) = this.coerceIn(-abs, abs)
infix fun Double.coIn(abs: Double) = this.coerceIn(-abs, abs)
val Double.percentI: Int
    get() = (this * 100).toInt()
val Float.percentI: Int
    get() = (this * 100).toInt()

fun <T> ArrayList(len: Int, gen: (Int) -> T) =
    ArrayList<T>(len).apply {
        for (i in 0 until len) {
            add(gen(i))
        }
    }

infix fun Int.between(end: Int): IntRange {
    return IntRange(this + 1, end - 1)
}

fun <T> Class<T>.EmptyArray(): Array<T> = JavaU.emptyArray(this)
val Boolean.Int: Int
    get() = if (this) 1 else 0
val Boolean.Float: Float
    get() = if (this) 1f else 0f

fun <T> Array<T>.equalsNoOrder(other: Array<T>): Boolean =
    JavaU.equalsNoOrder(this, other)

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

fun <T> Class<T>.newArray(size: Int): Array<T> {
    return JavaU.newArray(this, size)
}

inline fun <reified T> Array<T>.sortManually(vararg indices: Int): Array<T> {
    assert(indices.size == this.size)
    val clz = T::class.java
    val res = clz.newArray(this.size)
    for ((i, index) in indices.withIndex()) {
        res[i] = this[index]
    }
    return res
}

fun Boolean.toInt(): Int = if (this) 1 else 0
fun Boolean.toFloat(): Float = if (this) 1f else 0f
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

val Any.directSuperClz: Class<*>
    get() {
        val clz = javaClass
        return if (clz.isAnonymousClass) clz.superclass else clz
    }
