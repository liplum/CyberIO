package net.liplum.gradle.dsl

import groovy.lang.GroovyObject

operator fun GroovyObject?.invoke(name: String, vararg parameters: Pair<Any, Any?>) {
    this?.invokeMethod(name, mapOf(*parameters))
}

operator fun GroovyObject?.invoke(name: String, parameters: Map<Any, Any?>) {
    this?.invokeMethod(name, parameters)
}