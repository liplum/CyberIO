import net.liplum.gradle.tasks.CompileOpenGalTask

tasks {
    register("build") {
        group = "build"
        dependsOn("copyAllFiles")
        dependsOn("compileGAL")
    }
    register("copyAllFiles") {
        group = "copy"
        dependsOn("copyInfo")
        dependsOn("copyStories")
    }
    register("copyStories") {
        group = "copy"
        doLast {
            copy {
                from("stories") {
                    include("*.properties")
                }
                into("$rootDir/extra/stories")
            }
        }
    }
    register("copyInfo") {
        group = "copy"
        doLast {
            copy {
                from("ScriptInfo.json")
                into("$rootDir/extra")
            }
        }
    }
    register("compileGAL") {
        group = "build"
        doLast {
            create<CompileOpenGalTask>("${name}Wrapper") {
                args.set(listOf(
                    "-c",
                    "-recursive=true",
                    "-batch=folder",
                    "scripts",
                    "-t",
                    "$rootDir/extra/script"
                ))
            }.compile()
        }
    }
}