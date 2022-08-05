package net.liplum.processor.cacherw

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate
import net.liplum.processor.plusAssign
import java.io.File

/**
 * arc.util.io.Reads -> plumy.core.persistence.CacheReaderSpec
 * arc.util.io.Writes -> plumy.core.persistence.CacheWriter
 */
class CacheRwProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>,
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        // Filter target
        val rwFName = options["Cache.CacheRWQualifiedName"] ?: "net.liplum.annotations.CacheRW"
        val extension = options["Cache.Extension"] ?: "Cached"
        val readsMapping = options["Cache.ReadsMapping"] ?: "net.liplum.common.persistence.CacheReaderSpec"
        val writesMapping = options["Cache.WritesMapping"] ?: "net.liplum.common.persistence.CacheWriter"
        val rwSymbls = resolver
            .getSymbolsWithAnnotation(rwFName)
            .filterIsInstance<KSFile>()
        if (!rwSymbls.iterator().hasNext()) return emptyList()
        class RwVisitor : KSVisitorVoid() {
            override fun visitFile(file: KSFile, data: Unit) {
                val originalFile = file.fileName
                val mirrorFile = file.fileName.removeSuffix(".kt") + extension
                val out = codeGenerator.createNewFile(
                    dependencies = Dependencies(false),
                    packageName = file.packageName.asString(),
                    fileName = mirrorFile
                )
                val codes = File(file.filePath).readText()
                // Remove the annotation to prevent infinite rounds
                out += codes.replace("@file:CacheRW", "")
                    .replace("arc.util.io.Reads", readsMapping)
                    .replace("arc.util.io.Writes", writesMapping)
                    .replace("Reads", "CacheReaderSpec")
                    .replace("Writes", "CacheWriter")
                    .replace("Reads", readsMapping)
                    .replace("Writes", writesMapping)
                out.close()
                logger.info("Generated a mirror of $originalFile into $mirrorFile.kt successfully.")
            }
        }
        rwSymbls.forEach { it.accept(RwVisitor(), Unit) }
        return rwSymbls.filterNot { it.validate() }.toList()
    }
}
