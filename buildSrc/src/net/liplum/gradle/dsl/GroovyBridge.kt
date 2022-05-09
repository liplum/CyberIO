package net.liplum.gradle.dsl

import groovy.lang.GroovyObject

operator fun GroovyObject?.invoke(name: String, vararg parameters: Pair<Any, Any?>) {
    this?.invokeMethod(name, mapOf(*parameters))
}

operator fun GroovyObject?.invoke(name: String, parameters: Map<Any, Any?>) {
    this?.invokeMethod(name, parameters)
}

inline fun GroovyObject.call(spec: GroovyObjSpec.() -> Unit) {
    GroovyObjSpec(this).spec()
}
@JvmInline
value class GroovyObjSpec(
    val obj: GroovyObject,
) {
    operator fun String.invoke(vararg p: Pair<Any, Any?>) {
        obj(this, *p)
    }
}