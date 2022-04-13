@file:JvmName("ByteU")

package net.liplum.utils

infix fun Int.isOn(pos: Int): Boolean = (this shr pos and 1) != 0

infix fun Int.isOff(pos: Int): Boolean = (this shr pos and 1) == 0

infix fun Int.on(pos: Int): Int = this or (1 shl pos)

infix fun Int.off(pos: Int): Int = this and (1 shl pos).inv()

infix fun Int.reverse(pos: Int): Int = this and (1 shl pos)
