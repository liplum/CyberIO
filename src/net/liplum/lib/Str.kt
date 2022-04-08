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

fun buildFill(c: Char, number: Int): StringBuilder {
    val sb = StringBuilder()
    for (i in 0 until number) {
        sb.append(c)
    }
    return sb
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
fun String.CenterFillUntil(c: Char, totalChar: Int, leftAlign: Boolean = true): String {
    if (totalChar <= 0) return ""
    val len = this.length
    val rest = totalChar - len
    if (rest == 0) return this
    if (rest < 0) return this.substring(0, totalChar)
    val sb = StringBuilder()
    var leftChar = rest / 2
    var rightChar = rest / 2
    if (rest.isOdd) {
        if (leftAlign)
            rightChar++
        else
            leftChar++
    }
    for (i in 0 until leftChar) {
        sb.append(c)
    }
    sb.append(this)
    for (i in 0 until rightChar) {
        sb.append(c)
    }
    return sb.toString()
}
@JvmOverloads
fun String.buildCenterFillUntil(c: Char, totalChar: Int, leftAlign: Boolean = true): StringBuilder {
    if (totalChar <= 0) return StringBuilder()
    val len = this.length
    val rest = totalChar - len
    if (rest == 0) return StringBuilder(this)
    if (rest < 0) return StringBuilder(this.substring(0, totalChar))
    val sb = StringBuilder()
    var leftChar = rest / 2
    var rightChar = rest / 2
    if (rest.isOdd) {
        if (leftAlign)
            rightChar++
        else
            leftChar++
    }
    for (i in 0 until leftChar) {
        sb.append(c)
    }
    sb.append(this)
    for (i in 0 until rightChar) {
        sb.append(c)
    }
    return sb
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
    val s = StringBuilder()
    for (e in this) {
        s.append(e)
        s.append(' ')
    }
    return s.toString()
}