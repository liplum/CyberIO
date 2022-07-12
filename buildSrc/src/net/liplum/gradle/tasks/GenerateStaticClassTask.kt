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
import java.io.OutputStream

open class GenerateStaticClassTask : DefaultTask() {
    val jsonFile: Property<File> =
        project.objects.property(File::class.java)
        @Input get
    val args: MapProperty<String, String> =
        project.objects.mapProperty(String::class.java, String::class.java)
        @Optional @Input get

    companion object : HashMap<String, IClassConvert>()
    @TaskAction
    fun generate() {
        val jsonText = jsonFile.get().readText()
        val genArgs = if (args.isPresent) args.get() else emptyMap()
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
                override val converters = GenerateStaticClassTask
                override val args = genArgs
            })
        }
    }
}

val String.package2Path: String
    get() = this.replace(".", File.separator)

fun String.sub(sub: String): String =
    this + File.separator + sub