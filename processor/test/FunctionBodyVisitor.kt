package net.liplum

import com.tschuchort.compiletesting.SourceFile

val TestFunctionBody = SourceFile.kotlin(
    "TestFunctionBody.kt", """
package net.liplum
import net.liplum.annotations.CacheRW
@CacheRW("TestFileName")
fun testFunction(){
    val a = "abc"
    val c = a + 1
    if(c == "abc1"){
        println("yes")
    }
}
""".trimIndent()
)
