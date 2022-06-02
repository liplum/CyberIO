@file:JvmName("ReflectH")

package net.liplum.lib.utils

import java.lang.reflect.Method
import kotlin.reflect.KProperty

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

class ReflectObj<T>(
    val obj: Any
) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        return obj.getF(property.name)
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        obj.setF(property.name, value)
    }
}