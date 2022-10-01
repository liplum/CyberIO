@file:JvmName("ReflectH")

package net.liplum.common.util

import plumy.core.Out
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.*
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

fun <T> T.copyFieldsFrom(from: T) {
    ReflectU.copyFields(from, this)
}

fun Class<*>.getMethodBy(name: String, vararg argClz: Class<*>): Method {
    return ReflectU.getMethod(this, name, *argClz)
}

class ReflectObj<T>(
    val obj: Any,
) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        return obj.getF(property.name)
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        obj.setF(property.name, value)
    }
}

inline fun Class<*>.allFieldsIncludeParents(
    @Out out: MutableCollection<Field>,
    filter: (Field) -> Boolean = { true },
) {
    val entry = ReflectU.getEntry(this)
    val cur = entry.allFieldsIncludeParents
    if (cur != null) out.apply {
        cur.forEach {
            if (filter(it))
                out.add(it)
        }
        return
    }
    val fields = LinkedList<Field>()
    var curClz = if (isAnonymousClass) superclass else this
    while (curClz != null) {
        val allFields = ReflectU.getEntry(curClz).getAllFields()
        for (f in allFields) {
            f.isAccessible = true
            fields.add(f)
            if (filter(f)) {
                out.add(f)
            }
        }
        curClz = curClz.superclass
    }
    entry.allFieldsIncludeParents = fields
}