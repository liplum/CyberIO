package net.liplum

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode.OK
import com.tschuchort.compiletesting.kspArgs
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import net.liplum.processor.cacherw.CacheRwProcessorProv
import net.liplum.processor.dp.DpProcessorProv
import org.junit.jupiter.api.Test

class TestProcess {
    @Test
    fun `test process resolve dependency`() {
        val sourceFiles = listOf(
            Blocks,
            Items,
            Liquids
        )
        val compilation = KotlinCompilation().apply {
            sources = sourceFiles
            inheritClassPath = true
            kspArgs = HashMap(
                mapOf(
                    "PackageName" to "net.liplum",
                    "GenerateSpec" to "Contents"
                )
            )
            symbolProcessorProviders = listOf(DpProcessorProv())
            messageOutputStream = System.out
        }
        val result = compilation.compile()
        println(compilation.kspSourcesDir.absoluteFile)
        assert(result.exitCode == OK)
    }
    @Test
    fun `test process inspect function body`() {
        val sourceFiles = listOf(TestFunctionBody)
        val compilation = KotlinCompilation().apply {
            sources = sourceFiles
            inheritClassPath = true
            kspArgs = HashMap(mapOf())
            symbolProcessorProviders = listOf(CacheRwProcessorProv())
            messageOutputStream = System.out
        }
        val result = compilation.compile()
        assert(result.exitCode == OK)
    }
}
