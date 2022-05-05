package net.liplum.utils

import java.lang.reflect.Method

fun Any?.setF(name: String, value: Any?) {
    if (this != null) {
        ReflectU.set(this, name, value)
    }
}

fun Any?.setFIn(clz: Class<*>, name: String, value: Any?) {
    if (this != null) {
        ReflectU.set(clz, this, name, value)
    }
}

fun <T> Any.getF(name: String): T =
    ReflectU.get(this, name)

fun <T> Any.getFIn(clz: Class<*>, name: String): T =
    ReflectU.get(clz, this, name)

fun <T> Class<*>.getF(name: String): T =
    ReflectU.get(this, name)

fun <T> T.copyFrom(from: T) {
    ReflectU.copyFields(from, this)
}

fun Class<*>.getMethodBy(name: String, vararg argClz: Class<*>): Method {
    return ReflectU.getMethod(this, name, *argClz)
}