import net.liplum.gradle.tasks.DownloadTask

val outputJarName: String get() = extra["outputJarName"] as String
val mdtVersion: String get() = extra["mdtVersion"] as String
val mdtVersionNum: String get() = extra["mdtVersionNum"] as String
fun getOutputJar(): File? {
    val jarFile = File("${project(":main").buildDir}/libs/${outputJarName}Desktop.jar")
    if (!jarFile.exists()) {
        logger.lifecycle("Jar cannot be found at ${jarFile.path}")
        return null
    }
    return jarFile
}

fun copyJarFile(jarFile: File, modsFolder: File) {
    copy {
        from(jarFile)
        into(modsFolder)
    }
}
tasks {
    register("copyJar") {
        dependsOn(":main:jar")
        doLast {
            val jarFile = getOutputJar() ?: return@doLast
            if (org.gradle.internal.os.OperatingSystem.current().isWindows) {
                val APPDATA = System.getenv("APPDATA")
                val modsFolder = File("$APPDATA/Mindustry/mods")
                modsFolder.mkdirs()
                File(modsFolder,"$outputJarName.jar").delete()
                File(modsFolder,"${outputJarName}Desktop.jar").delete()
                copyJarFile(jarFile, modsFolder)
            }
        }
    }
    register("copyJarServer") {
        dependsOn(":main:jar")
        doLast {
            val jarFile = getOutputJar() ?: return@doLast
            val modsFolder = File("/config/mods")
            copyJarFile(jarFile, modsFolder)
        }
    }
    register("downloadDesktop") {
        group = "download"
        doLast {
            create<DownloadTask>("${name}Wrapper") {
                sourceUrl.set("http://github.com/Anuken/Mindustry/releases/download/${mdtVersion}/Mindustry.jar")
                targetFile.set(File("$rootDir/run/Mindustry${mdtVersionNum}.jar"))
                tip.set("Downloading Mindustry ${mdtVersion}...")
            }.download()
        }
    }

    register("downloadServer") {
        group = "download"
        doLast {
            create<DownloadTask>("${name}Wrapper") {
                sourceUrl.set("https://github.com/Anuken/Mindustry/releases/download/${mdtVersion}/server-release.jar")
                targetFile.set(File("$rootDir/run/MindustryServer${mdtVersionNum}.jar"))
                tip.set("Downloading Mindustry Server ${mdtVersion}...")
            }.download()
        }
    }

    register<JavaExec>("runMod") {
        group = "run"
        dependsOn("copyJar")
        dependsOn("downloadDesktop")
        val gameFile = File("$rootDir/run/Mindustry${mdtVersionNum}.jar")
        mainClass.set("-jar")
        args = listOf(gameFile.path)
    }

    register<JavaExec>("runGame") {
        group = "run"
        dependsOn("downloadDesktop")
        val gameFile = File("$rootDir/run/Mindustry${mdtVersionNum}.jar")
        mainClass.set("-jar")
        args = listOf(gameFile.path)
    }

    register<JavaExec>("runModServer") {
        group = "run"
        dependsOn("copyJarServer")
        dependsOn("downloadServer")
        val gameFile = File("$rootDir/run/MindustryServer${mdtVersionNum}.jar")
        mainClass.set("-jar")
        standardInput = System.`in`
        args = listOf(gameFile.path)
    }

    register<JavaExec>("runServer") {
        group = "run"
        dependsOn("downloadServer")
        val gameFile = File("$rootDir/run/MindustryServer${mdtVersionNum}.jar")
        mainClass.set("-jar")
        standardInput = System.`in`
        args = listOf(gameFile.path)
    }
    register("runModBoth") {
        group = "run"
        dependsOn("downloadServer")
        dependsOn("copyJarServer")
        doLast {
            val client = Thread {
                named<JavaExec>("runMod").get().exec()
            }
            val server = Thread {
                named<JavaExec>("runModServer").get().exec()
            }
            server.start()
            client.start()
            server.join()
            client.join()
        }
    }
}