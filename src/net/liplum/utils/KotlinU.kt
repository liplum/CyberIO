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