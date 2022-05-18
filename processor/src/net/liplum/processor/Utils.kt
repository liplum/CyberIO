package net.liplum.processor

import java.io.OutputStream

operator fun OutputStream.plusAssign(str: String) {
    this.write(str.toByteArray())
}

fun OutputStream.line() {
    this += "\n"
}

fun String.simpleName() =
    split('.').last()