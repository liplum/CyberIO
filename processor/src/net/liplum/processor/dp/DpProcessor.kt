package net.liplum.processor.dp

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import net.liplum.processor.plusAssign
import net.liplum.processor.simpleName

class DpProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>,
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val dependOnFullName = options["Dp.DependOnQualifiedName"] ?: "net.liplum.annotations.DependOn"
        val dependOnShortName = dependOnFullName.simpleName()
        val symbols = resolver
            .getSymbolsWithAnnotation(dependOnFullName)
            .filterIsInstance<KSFunctionDeclaration>()
        if (!symbols.iterator().hasNext()) return emptyList()
        val packageName = options["Dp.PackageName"] ?: "net.liplum.gen"
        val fileName = options["Dp.FileName"] ?: "GeneratedDpFile"
        val file = codeGenerator.createNewFile(
            dependencies = Dependencies(false),
            packageName = packageName,
            fileName = fileName
        )
        if (packageName.isNotEmpty())
            file += "package $packageName\n"
        val scope = options["Dp.Scope"] ?: packageName
        val spec = options["Dp.GenerateSpec"] ?: ""
        val useTopLevel = spec.isEmpty()
        if (!useTopLevel) {
            // Start object $spec
            file += "object $spec{\n"
        }
        val genFuncName = options["Dp.GeneratedFunctionName"] ?: "load"
        // Start function $genFuncName()
        file += "fun $genFuncName(){\n"
        val graph = DpGraph()
        var counter = 0

        class Visitor : KSVisitorVoid() {
            override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
                if (function.parameters.isNotEmpty()) {
                    logger.error("Only allow zero-argument Function in @DependOn", function)
                    return
                }
                val annotation: KSAnnotation = function.annotations.first {
                    it.shortName.asString() == dependOnShortName
                }
                val dependenciesArg: KSValueArgument = annotation.arguments.first {
                    it.name?.asString() == "dependencies"
                }
                val curFuncFullName = function.qualifiedName?.asString()
                if (curFuncFullName != null) {
                    @Suppress("UNCHECKED_CAST")
                    val dependencies = dependenciesArg.value as ArrayList<String>
                    if (dependencies.isEmpty()) {
                        graph[curFuncFullName]
                        logger.info("${function.simpleName.asString()} has no dependency.")
                    } else {
                        for (dependency in dependencies) {
                            val dpFullName = "$scope.$dependency"
                            graph[curFuncFullName].dependsOn(graph[dpFullName])
                        }
                        logger.info("${function.simpleName.asString()} has $dependencies.")
                    }
                    counter++
                } else {
                    logger.error("${function.simpleName.asString()} doesn't have a full name", function)
                }
            }
        }
        symbols.forEach { it.accept(Visitor(), Unit) }
        try {
            val functions = graph.resolveAllInOrder()
            for (func in functions) {
                logger.info("[DpProcessor] ${func.id}.")
            }
            val qualifiers = functions.mapNotNull {
                val split = it.id.split(".")
                when (split.size) {
                    0 -> null
                    1 -> null
                    else -> split.subList(0, split.size - 1).joinToString(".")
                }
            }.distinct()
            if (counter != functions.size) {
                logger.warn("There is any entry missing. Excepted: $counter != Actual: ${functions.size} ")
            }
            for (qualifier in qualifiers) {
                file += "// $qualifier\n"
            }
            for (func in functions) {
                file += "${func.id}()\n"
            }
            logger.info("Totally generated ${functions.size} ones.")
        } catch (e: Exception) {
            logger.error("Can't resolve dependencies because ${e.javaClass} ${e.message}")
            throw e
        }
        file += "}\n"
        if (!useTopLevel) {
            // End object $spec
            file += "}\n"
        }
        file.close()

        return symbols.filterNot { it.validate() }.toList()
    }
}