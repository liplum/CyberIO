import net.liplum.gradle.DownloadTask

val plumyVersion: String by extra(property("OpenGalPlumy") as String)

tasks {
    register("downloadOpenGalPlumy") {
        group = "download"
        doLast {
            create<DownloadTask>("${name}Wrapper") {
                sourceUrl.set("https://github.com/liplum/OpenGalPlumy/releases/download/$plumyVersion/PlumyCompiler.jar")
                targetFile.set(File("$rootDir/run/PlumyCompiler$plumyVersion.jar"))
                tip.set("Downloading OpenGal Plumy...")
            }.download()
        }
    }
    register<JavaExec>("compileGAL") {
        group = "build"
        dependsOn("classes")
        dependsOn("downloadOpenGalPlumy")
        val plumy = File("$rootDir/run/PlumyCompiler$plumyVersion.jar")
        mainClass.set("-jar")
        standardInput = System.`in`
        args = listOf(
            plumy.path,
            "-c",
            "-recursive=true",
            "-batch=folder",
            "scripts",
            "-t",
            "assets/script"
        )
    }
}