package net.liplum.gradle

import net.liplum.gradle.dsl.call
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class DownloadTask : DefaultTask() {
    @get:Input
    abstract val sourceUrl: Property<String>
    @get:Input
    abstract val overwrite: Property<Boolean>
    abstract val tip: Property<String>
        @Optional @Input get
    @get:OutputFile
    abstract val targetFile: Property<File>

    init {
        overwrite.convention(false)
    }
    @TaskAction
    fun download() {
        val targetFile = targetFile.get()
        val sourceUrl = sourceUrl.get()
        if (!targetFile.exists() || overwrite.getOrElse(false)) {
            targetFile.parentFile?.mkdirs()
            if (tip.isPresent) logger.lifecycle(tip.get())
            logger.lifecycle("Downloading file from $sourceUrl into ${targetFile.path}, please wait for a while.")
            ant.call {
                "get"("src" to sourceUrl, "dest" to targetFile)
            }
        } else {
            logger.info("$targetFile exists but there is no need to overwrite it.")
        }
    }
}