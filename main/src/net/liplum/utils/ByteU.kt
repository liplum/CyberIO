@file:JvmName("ByteU")

package net.liplum.utils

/**
 * **[pos] Starts with 0**
 * @return whether the [pos] of bits is 1.
 */
infix fun Int.isOn(pos: Int): Boolean = (this shr pos and 1) != 0
/**
 * **[pos] Starts with 0**
 * @return whether the [pos] of bits is 0.
 */
infix fun Int.isOff(pos: Int): Boolean = (this shr pos and 1) == 0
/**
 * Set 1 on [pos]
 *
 * **[pos] Starts with 0**
 */
infix fun Int.on(pos: Int): Int = this or (1 shl pos)
/**
 * Set 0 on [pos]
 *
 * **[pos] Starts with 0**
 */
infix fun Int.off(pos: Int): Int = this and (1 shl pos).inv()
/**
 * Reverse the bit on [pos]
 *
 * **[pos] Starts with 0**
 */
infix fun Int.reverse(pos: Int): Int = this and (1 shl pos)
