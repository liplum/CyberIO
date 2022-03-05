package net.liplum.utils

fun Any?.set(name: String, value: Any?) {
    if (this != null) {
        ReflectU.set(this, name, value)
    }
}

fun <T> Any.get(name: String): T =
    ReflectU.get(this, name)