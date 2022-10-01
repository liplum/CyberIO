package net.liplum.math

import arc.math.Mathf

/**
 * Roll a number belongs to `[0,this)`
 * @return the result that doesn't equal to [exception]. Otherwise, -1 will be returned.
 */
fun Int.randomExcept(exception: Int): Int {
    var res: Int
    when (val len = this.coerceAtLeast(0)) {
        0 -> res = -1
        1 -> res = 0
        else -> do {
            res = Mathf.random(0, len - 1)
        } while (res == exception)
    }
    return res
}
/**
 * Random select an element by its weight
 * @param weights it.size == list.size.
 */
fun <T> List<T>.randomByWeights(
    weights: IntArray,
    maxWeight: Int = weights.last(),
): T {
    assert(weights.size == size) { "Weights' size(${weights.size}) don't match receiver's size($size)" }
    var pos = Mathf.random(maxWeight - 1)
    for (i in indices) {
        val weight = weights[i]
        if (pos < weight) {
            return this[i]
        }
        pos -= weights[i]
    }
    throw ArithmeticException("Random weight($pos) is over than maximum($maxWeight)")
}
