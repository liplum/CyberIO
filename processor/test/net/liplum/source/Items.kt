package net.liplum.source

import com.tschuchort.compiletesting.SourceFile

val Items = SourceFile.kotlin("Blocks.kt","""
package net.liplum
import net.liplum.annotations.DependOn
object Items {
    lateinit var stick: Any
    lateinit var bottle: Any
    lateinit var backpack: Any
    lateinit var lighter: Any
    @DependOn("Blocks.grass", "Blocks.tree")
    fun stick() {
    }
    @DependOn("Blocks.sand","Liquids.water")
    fun bottle() {
    }
    @DependOn("Blocks.grass")
    fun waterPurifier() {
    }
    @DependOn("Blocks.tree")
    fun lighter() {
    }
}
""".trimIndent()
)