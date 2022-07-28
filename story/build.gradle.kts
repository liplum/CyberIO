tasks {
    register("build") {
        group = "build"
        dependsOn("copyAllFiles")
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
}