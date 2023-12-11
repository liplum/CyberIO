package net.liplum

import com.tschuchort.compiletesting.SourceFile

val Blocks = SourceFile.kotlin(
    "Blocks.kt", """
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
val Items = SourceFile.kotlin(
    "Blocks.kt", """
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
val Liquids = SourceFile.kotlin(
    "Liquids.kt", """
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