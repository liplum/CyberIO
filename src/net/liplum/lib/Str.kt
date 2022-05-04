package net.liplum.lib

import net.liplum.utils.isOdd
import org.jetbrains.annotations.Contract

fun Fill(c: Char, number: Int): String {
    val sb = StringBuilder()
    for (i in 0 until number) {
        sb.append(c)
    }
    return sb.toString()
}

fun buildFill(c: Char, number: Int): StringBuilder =
    StringBuilder().buildFill(c, number)

fun StringBuilder.buildFill(c: Char, number: Int): StringBuilder {
    for (i in 0 until number) {
        append(c)
    }
    return this
}
@Contract(pure = true)
fun String.CenterFill(c: Char, number: Int): String {
    if (number <= 0) return this
    val sb = StringBuilder()
    for (i in 0 until number) {
        sb.append(c)
    }
    sb.append(this)
    for (i in 0 until number) {
        sb.append(c)
    }
    return sb.toString()
}
@Contract(pure = true)
@JvmOverloads
fun String.CenterFillUntil(c: Char, totalChar: Int, leftAlign: Boolean = true): String =
    this.buildCenterFillUntil(c, totalChar, leftAlign).toString()
@JvmOverloads
fun String.buildCenterFillUntil(
    c: Char,
    totalChar: Int,
    leftAlign: Boolean = true
): StringBuilder =
    StringBuilder().buildCenterFillUntil(this, c, totalChar, leftAlign)
@JvmOverloads
fun StringBuilder.buildCenterFillUntil(
    title: String,
    c: Char,
    totalChar: Int,
    leftAlign: Boolean = true
): StringBuilder {
    if (totalChar <= 0) return StringBuilder()
    val len = title.length
    val rest = totalChar - len
    if (rest == 0) return StringBuilder(title)
    if (rest < 0) return StringBuilder(title.substring(0, totalChar))
    var leftChar = rest / 2
    var rightChar = rest / 2
    if (rest.isOdd) {
        if (leftAlign)
            rightChar++
        else
            leftChar++
    }
    for (i in 0 until leftChar) {
        append(c)
    }
    append(title)
    for (i in 0 until rightChar) {
        append(c)
    }
    return this
}

infix fun StringBuilder.addLeft(str: String): StringBuilder {
    this.insert(0, str)
    return this
}

infix fun StringBuilder.addRight(str: String): StringBuilder {
    this.append(str)
    return this
}

fun Array<out Any>.toLinkedString(): String {
    if (this.isEmpty()) return ""
    val s = StringBuilder()
    for (e in this) {
        s.append(e)
        s.append(' ')
    }
    return s.toString()
}

fun Collection<Any>.toLinkedString(): String {
    if (this.isEmpty()) return ""
    val s = StringBuilder()
    for (e in this) {
        s.append(e)
        s.append(' ')
    }
    return s.toString()
}