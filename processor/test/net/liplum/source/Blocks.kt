package net.liplum.source

import com.tschuchort.compiletesting.SourceFile

val Blocks = SourceFile.kotlin("Blocks.kt","""
package net.liplum
import net.liplum.annotations.DependOn
object Blocks {
    lateinit var grass: Any
    lateinit var tree: Any
    lateinit var sand: Any
    lateinit var dirt: Any
    @DependOn
    fun grass() {
    }
    @DependOn
    fun tree() {
    }
    @DependOn
    fun sand() {
    }
    @DependOn
    fun dirt() {
    }
}
""".trimIndent()
)