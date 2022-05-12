package net.liplum.source

import com.tschuchort.compiletesting.SourceFile

val Liquids = SourceFile.kotlin("Liquids.kt", """
package net.liplum
import net.liplum.annotations.DependOn
object Liquids {
    lateinit var water: Any
    lateinit var saltWater: Any
    lateinit var lava: Any
    lateinit var milk: Any
    @DependOn
    fun water() {
    }
    @DependOn
    fun saltWater() {
    }
    @DependOn
    fun lava() {
    }
    @DependOn
    fun milk() {
    } 
}
""".trimIndent()
)