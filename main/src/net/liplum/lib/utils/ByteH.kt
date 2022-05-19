@file:JvmName("ByteH")

package net.liplum.lib.utils

import net.liplum.lib.Out
import java.nio.ByteBuffer
import kotlin.math.ceil

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
infix fun Byte.shl(that: Int): Int = this.toInt().shl(that)
infix fun Int.shl(that: Byte): Int = this.shl(that.toInt())
infix fun Byte.shl(that: Byte): Int = this.toInt().shl(that.toInt())
infix fun Byte.shr(that: Int): Int = this.toInt().shr(that)
infix fun Int.shr(that: Byte): Int = this.shr(that.toInt())
infix fun Byte.shr(that: Byte): Int = this.toInt().shr(that.toInt())
infix fun Short.shl(that: Int): Int = this.toInt().shl(that)
infix fun Int.shl(that: Short): Int = this.shl(that.toInt())
infix fun Short.shl(that: Short): Int = this.toInt().shl(that.toInt())
infix fun Short.shr(that: Int): Int = this.toInt().shr(that)
infix fun Int.shr(that: Short): Int = this.shr(that.toInt())
infix fun Short.shr(that: Short): Int = this.toInt().shr(that.toInt())
infix fun Byte.and(that: Int): Int = this.toInt().and(that)
infix fun Int.and(that: Byte): Int = this.and(that.toInt())
infix fun Byte.and(that: Byte): Int = this.toInt().and(that.toInt())
/*
No validation
fun ByteArray.toLongArray(@Out outArr: LongArray) {
    val padding: Int = outArr.size * 8 - this.size
    val buffer = ByteBuffer.allocate(this.size + padding)
        .put(ByteArray(padding)).put(this)
    buffer.flip()
    buffer.asLongBuffer().get(outArr)
}

fun ByteArray.toLongArray(): LongArray {
    return LongArray(ceil(this.size / 4.0).toInt())
}*/
