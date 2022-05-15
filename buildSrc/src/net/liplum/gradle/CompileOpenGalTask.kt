package net.liplum.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import net.liplum.plumy.main as Plumy

abstract class CompileOpenGalTask : DefaultTask() {
    @get:Input
    abstract val args: ListProperty<String>
    @TaskAction
    fun compile() {
        Plumy(args.get().toTypedArray())
    }
}