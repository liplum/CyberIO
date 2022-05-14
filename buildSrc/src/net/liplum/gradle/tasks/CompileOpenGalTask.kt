package net.liplum.gradle.tasks

import net.liplum.plumy.main
import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

abstract class CompileOpenGalTask : DefaultTask() {
    @get:Input
    abstract val args: ListProperty<String>
    @TaskAction
    fun compile() {
        main(args.get().toTypedArray())
    }
}