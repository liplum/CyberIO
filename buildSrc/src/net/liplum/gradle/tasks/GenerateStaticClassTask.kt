package net.liplum.gradle.tasks

import net.liplum.gradle.gen.IClassConvert
import net.liplum.gradle.gen.IFileHandler
import net.liplum.gradle.gen.IGeneratorContext
import net.liplum.gradle.gen.StaticClassGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.FileNotFoundException
import java.io.OutputStream

abstract class GenerateStaticClassTask : DefaultTask() {
    @get:Input
    abstract val jsonPath: Property<String>
    /**
     * If enabled parallel, it doesn't guarantee the order of fields.
     */
    abstract val useParallel: Property<Boolean>
        @Optional @Input get
    abstract val args: MapProperty<String, String>
        @Optional @Input get
    abstract val converters: MapProperty<String, IClassConvert>
        @Optional @Input get

    init {
        useParallel.convention(false)
    }
    @TaskAction
    fun generate() {
        val path = jsonPath.get()
        val jsonFile = File(path).apply {
            if (!isFile || !exists()) throw FileNotFoundException("$this")
        }
        val jsonText = jsonFile.readText()
        val genArgs = if (args.isPresent) args.get() else emptyMap()
        val genConverters = if (converters.isPresent) converters.get() else emptyMap()
        StaticClassGenerator().apply {
            generateClass(jsonText, object : IGeneratorContext {
                override val fileHandler = object : IFileHandler {
                    override fun createJavaFile(`package`: String, file: String): OutputStream =
                        File(
                            project.buildDir.toString()
                                .sub("generated")
                                .sub("classGen")
                                .sub(`package`.package2Path.sub("$file.java"))
                        ).run {
                            parentFile.mkdirs()
                            createNewFile()
                            outputStream()
                        }
                }
                override val useParallel = this@GenerateStaticClassTask.useParallel.get()
                override val converters = genConverters
                override val args = genArgs
            })
        }
    }
}

val String.package2Path: String
    get() = this.replace(".", File.separator)

fun String.sub(sub: String): String =
    this + File.separator + sub