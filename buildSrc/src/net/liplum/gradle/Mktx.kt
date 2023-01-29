package net.liplum.gradle

import org.gradle.api.artifacts.dsl.DependencyHandler

fun DependencyHandler.mktxApi(version: String, vararg modules: String) {
    for (module in modules) {
        val notation = "com.github.plumygames.mktx:$module:$version"
        add("api", notation)
        add("testApi", notation)
    }
}

fun DependencyHandler.mktxImplmenetation(version: String, vararg modules: String) {
    for (module in modules) {
        val notation = "com.github.plumygames.mktx:$module:$version"
        add("implementation", notation)
        add("testImplementation", notation)
    }
}