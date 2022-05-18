package net.liplum

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode.OK
import com.tschuchort.compiletesting.kspArgs
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import net.liplum.processor.dp.DpProcessorProv
import net.liplum.source.Blocks
import net.liplum.source.Items
import net.liplum.source.Liquids
import org.junit.jupiter.api.Test

class TestProcess {
    @Test
    fun `test process`() {
        val sourceFiles = listOf(
            Blocks,
            Items,
            Liquids
        )
        val compilation = KotlinCompilation().apply {
            sources = sourceFiles
            inheritClassPath = true
            kspArgs = HashMap(mapOf(
                "PackageName" to "net.liplum",
                "GenerateSpec" to "Contents"
            ))
            symbolProcessorProviders = listOf(DpProcessorProv())
            messageOutputStream = System.out
        }
        val result = compilation.compile()
        println(compilation.kspSourcesDir.absoluteFile)
        assert(result.exitCode == OK)
    }
}
