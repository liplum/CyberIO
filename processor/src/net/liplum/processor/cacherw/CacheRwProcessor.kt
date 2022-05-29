package net.liplum.processor.cacherw

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import net.liplum.processor.simpleName
import java.io.Closeable
import java.io.File
import java.io.OutputStream

class CacheRwProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>,
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        // Filter target
        val rwFName = options["Cache.CacheRWQualifiedName"] ?: "net.liplum.annotations.CacheRW"
        val rwSName = rwFName.simpleName()
        val rwSymbls = resolver
            .getSymbolsWithAnnotation(rwFName)
            .filterIsInstance<KSFunctionDeclaration>()
        if (!rwSymbls.iterator().hasNext()) return emptyList()
        val locator2Content = HashMap<FunctionLocator,FileContent>()
        val fileTextCache = HashMap<String,String>()
        class RwVisitor : KSVisitorVoid() {
            override fun visitFile(file: KSFile, data: Unit) {
                super.visitFile(file, data)
            }

            override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
                val file = function.containingFile
                if(file != null){
                    val path = file.filePath
                    val content = fileTextCache.getOrPut(path) {
                        File(path).readText()
                    }
                }
            }
        }
        rwSymbls.forEach { it.accept(RwVisitor(), Unit) }
        return rwSymbls.filterNot { it.validate() }.toList()
    }
}

data class FunctionLocator(
    val packageName: String,
    val fileName: String,
)

class FileContent(
    val file: OutputStream
) : Closeable {
    override fun close() {
        file.close()
    }
}

