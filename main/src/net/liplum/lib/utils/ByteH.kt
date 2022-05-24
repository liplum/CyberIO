@file:JvmName("ByteH")

package net.liplum.lib.utils

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
/**
 * Assume this int is a short, it can get the little endian 8 bits=1 byte.
 * ## Like
 * - input => 52 1c
 * - output => 1c
 */
val Int.littleEndianByte: Int
    get() = this and 0xff
/**
 * Assume this int is a short, it can get the little endian 8 bits=1 byte.
 * ## Use case
 * - input => 52 1c
 * - output => 52
 */
val Int.bigEndianByte: Int
    get() = (this shr 8) and 0xff
/**
 * Assume this int is a short, it can get the first 8 bits=1 byte.
 * ## Use case
 * - input => 52 1c
 * - output => 1c
 */
val Int.littleEndianByteB: Byte
    get() = (this and 0xff).toByte()
/**
 * Assume this int is a short, it can get the little endian 8 bits=1 byte.
 * ## Use case
 * - input => 52 1c
 * - output => 52
 */
val Int.bigEndianByteB: Byte
    get() = ((this shr 8) and 0xff).toByte()
/**
 * @param big big endian 8 bits
 * @param little little endian 8 bits
 * ## Use case
 * - input => b1=52,b2=1c
 * - output => 1c 52
 * - output => last first
 */
fun twoBytesToShort(big: Byte, little: Byte): Short =
    (big shl 8 or (little and 0xFF)).toShort()
/**
 * @param big big endian 8 bits
 * @param little little endian 8 bits
 * ## Use case
 * - input => b1=52,b2=1c
 * - output => 1c 52
 * - output => last first
 */
fun twoBytesToShort(big: Int, little: Int): Int =
    big shl 8 or (little and 0xFF)