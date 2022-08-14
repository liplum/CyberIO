package net.liplum.processor

import com.google.devtools.ksp.symbol.KSAnnotation
import java.io.OutputStream

operator fun OutputStream.plusAssign(str: String) {
    this.write(str.toByteArray())
}

fun OutputStream.line() {
    this += "\n"
}

fun String.simpleName() =
    split('.').last()

operator fun String.times(times: Int) =
    this.repeat(times)

fun String.pascalCase(): String {
    if (this.isEmpty()) return ""
    val sb = StringBuilder()
    sb += this[0].uppercase()
    if (this.length > 1)
        sb += this.substring(1)
    return sb.toString()
}

operator fun StringBuilder.plusAssign(str: String) {
    this.append(str)
}

operator fun StringBuilder.plusAssign(c: Char) {
    this.append(c)
}

fun KSAnnotation.findParam(name: String) =
    arguments.first { it.name?.asString() == name }