@file:JvmName("ByteH")

package net.liplum.common.util

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
/**
 * Little Endian
 * Write a byte into the byte array at the offset
 * @param offset the offset start to write
 * @param value the byte to be written
 * @return the new offset after writing [value].
 * And with the new offset, you can continue to write more.
 */
fun ByteArray.writeByte(value: Byte, offset: Int = 0): Int {
    this[offset] = value
    return offset + 1
}
/**
 * Little Endian
 * Write a short into the byte array at the offset
 * @param offset the offset start to write
 * @param value the short to be written
 * @return the new offset after writing [value].
 * And with the new offset, you can continue to write more.
 */
fun ByteArray.writeShort(value: Short, offset: Int = 0): Int {
    for (i in 0 until 2) {
        this[offset + i] = (value shr (8 * i)).toByte()
    }
    return offset + 2
}
/**
 * Little Endian
 * Write an int into the byte array at the offset
 * @param offset the offset start to write
 * @param value the int to be written
 * @return the new offset after writing [value].
 * And with the new offset, you can continue to write more.
 */
fun ByteArray.writeInt(value: Int, offset: Int = 0): Int {
    for (i in 0 until 4) {
        this[offset + i] = (value shr (8 * i)).toByte()
    }
    return offset + 4
}
/**
 * Little Endian
 * Write a long into the byte array at the offset
 * @param offset the offset start to write
 * @param value the long to be written
 * @return the new offset after writing [value].
 * And with the new offset, you can continue to write more.
 */
fun ByteArray.writeLong(value: Long, offset: Int = 0): Int {
    for (i in 0 until 8) {
        this[offset + i] = (value shr (8 * i)).toByte()
    }
    return offset + 8
}
/**
 * Little Endian
 * Read a byte from the byte array at the offset
 * @param offset the offset start to read
 * @param cons to consume this byte
 * @return the new offset after reading.
 * And with the new offset, you can continue to read more.
 */
inline fun ByteArray.readByte(offset: Int = 0, cons: (Byte) -> Unit): Int {
    cons(this[offset])
    return offset + 1
}
/**
 * Little Endian
 * Read a short from the byte array at the offset
 * @param offset the offset start to read
 * @param cons to consume this short
 * @return the new offset after reading.
 * And with the new offset, you can continue to read more.
 */
inline fun ByteArray.readShort(offset: Int = 0, cons: (Short) -> Unit): Int {
    cons(((this[offset] and 0xff) or (this[offset + 1] shr 8) and 0xff).toShort())
    return offset + 1
}
/**
 * Little Endian
 * Read an int from the byte array at the offset
 * @param offset the offset start to read
 * @param cons to consume this int
 * @return the new offset after reading.
 * And with the new offset, you can continue to read more.
 */
inline fun ByteArray.readInt(offset: Int = 0, cons: (Int) -> Unit): Int {
    var result = 0
    for (i in 0 until 8) {
        result = result shl 8
        result = result or (this[offset + i] and 0xFF)
    }
    cons(result)
    return offset + 4
}
/**
 * Little Endian
 * Read a long from the byte array at the offset
 * @param offset the offset start to read
 * @param cons to consume this long
 * @return the new offset after reading.
 * And with the new offset, you can continue to read more.
 */
inline fun ByteArray.readLong(offset: Int = 0, cons: (Long) -> Unit): Int {
    var result: Long = 0
    for (i in 0 until 8) {
        result = result shl 8
        result = result or (this[offset + i] and 0xFF).toLong()
    }
    cons(result)
    return offset + 8
}