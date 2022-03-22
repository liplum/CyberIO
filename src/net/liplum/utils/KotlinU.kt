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