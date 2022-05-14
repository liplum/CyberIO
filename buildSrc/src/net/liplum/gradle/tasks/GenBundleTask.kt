package net.liplum.gradle.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

// TODO: Generate all empty bundle pairs by a default bundle.
abstract class GenBundleTask : DefaultTask() {
    @get:Input
    abstract val bundleDir: Property<File>
    var pattern: (String) -> String = { "" }
    @get:Input
    abstract val default: Property<String>
    init {
        default.convention("en")
    }
    @TaskAction
    fun gen() {
    }
}