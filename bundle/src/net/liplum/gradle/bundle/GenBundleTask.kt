package net.liplum.gradle.bundle

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import java.io.File

abstract class GenBundleTask : DefaultTask() {
    @get:Input
    abstract val bundleDir: Property<File>

}