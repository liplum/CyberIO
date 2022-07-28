package net.liplum.common

import arc.util.Strings
import arc.util.Time
import net.liplum.common.util.isOdd
import org.jetbrains.annotations.Contract

fun Fill(c: Char, number: Int): String {
    val sb = StringBuilder()
    for (i in 0 until number) {
        sb.append(c)
    }
    return sb.toString()
}

fun String.CoerceLength(number: Int, filler: Char = ' '): String {
    if (length == number) return this
    if (length > number) return substring(0, number)
    val sb = StringBuilder()
    sb.append(this)
    for (i in 0 until number - length) {
        sb.append(filler)
    }
    return sb.toString()
}

fun fill(number: Int, filler: Char): StringBuilder =
    StringBuilder().fill(number, filler)

fun StringBuilder.fill(number: Int, filler: Char = ' '): StringBuilder {
    for (i in 0 until number) {
        append(filler)
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
    this.BuildCenterFillUntil(c, totalChar, leftAlign).toString()
@JvmOverloads
fun String.BuildCenterFillUntil(
    c: Char,
    totalChar: Int,
    leftAlign: Boolean = true,
): StringBuilder =
    StringBuilder().BuildCenterFillUntil(this, c, totalChar, leftAlign)
@JvmOverloads
fun StringBuilder.BuildCenterFillUntil(
    title: String,
    c: Char,
    totalChar: Int,
    leftAlign: Boolean = true,
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

val String.ing: String
    get() = this + Strings.animated(Time.time, 4, 11f, ".")

fun String.segmentLines(maxPerLine: Int): String {
    val s = StringBuilder()
    for ((i, c) in this.withIndex()) {
        s.append(c)
        if ((i + 1) % maxPerLine == 0) {
            s.append('\n')
        }
    }
    return s.toString()
}

inline fun String.insertLineNumber(style: (Int) -> String): String {
    val s = StringBuilder()
    var line = 1
    s.append(style(line))
    for (c in this) {
        if (c == '\n') {
            line++
            s.append('\n')
            s.append(style(line))
        } else {
            s.append(c)
        }
    }
    return s.toString()
}