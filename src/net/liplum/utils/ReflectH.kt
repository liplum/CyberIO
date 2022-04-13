package net.liplum.utils

import java.lang.reflect.Method

fun Any?.setF(name: String, value: Any?) {
    if (this != null) {
        ReflectU.set(this, name, value)
    }
}

fun <T> Any.getF(name: String): T =
    ReflectU.get(this, name)

fun <T> T.copyFrom(from: T) {
    ReflectU.copyFields(from, this)
}

fun Class<*>.getMethodBy(name: String, vararg argClz: Class<*>): Method {
    return ReflectU.getMethod(this, name, *argClz)
}