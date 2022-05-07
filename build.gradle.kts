import org.gradle.internal.os.OperatingSystem
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import java.io.ByteArrayOutputStream

// Add any plugin you want
plugins {
    kotlin("jvm") version "1.6.10"
    application
    java
}
val mdtVersion by extra(property("MindustryVersion") as String)
val plumyVersion by extra(property("OpenGalPlumy") as String)
val mdtVersionNum by extra(mdtVersion.replace("v", ""))
val kotlinVersion by extra(property("KotlinVersion") as String)
val sdkRoot: String? by extra(System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT"))
val archivesBaseName by lazy { base.archivesName.get() }
sourceSets {
    main {
        java.srcDir("src")
    }
    test {
        java.srcDir("src")
    }
}
version = "3.4"
group = "net.liplum"
sourceSets {
    main {
        java.srcDir("src")
    }
    test {
        java.srcDir("src")
    }
}
java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://www.jitpack.io")
    }
}

dependencies {
    compileOnly("com.github.Anuken.Arc:arc-core:$mdtVersion")
    compileOnly("com.github.Anuken.Mindustry:core:$mdtVersion")
    implementation("com.github.liplum:OpenGAL:v0.4.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")

    testImplementation("com.github.Anuken.Arc:arc-core:$mdtVersion")
    testImplementation("com.github.Anuken.Mindustry:core:$mdtVersion")
    testImplementation("com.github.Anuken.Mindustry:desktop:$mdtVersion")
    testImplementation("com.github.Anuken.Mindustry:server:$mdtVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
    testImplementation("com.github.liplum:TestUtils:v0.1")
    // annotationProcessor "com.github.Anuken:jabel:$jabelVersion"
}
fun getOutputJar(): File? {
    val jarFile = File("$buildDir/libs/${archivesBaseName}Desktop.jar")
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

abstract class DownloadTask : DefaultTask() {
    @get:Input
    abstract val sourceUrl: Property<String>
    abstract val overwrite: Property<Boolean>
        @Optional @Input get
    @get:OutputFile
    abstract val targetFile: Property<File>
    @TaskAction
    fun download() {
        if (!targetFile.get().exists() || overwrite.getOrElse(false)) {
            logger.lifecycle("Downloading file from $sourceUrl into ${targetFile.get().path}, please wait for a while.")
            ant.invokeMethod("get", mapOf(
                "src" to sourceUrl, "dest" to targetFile
            ))
        }
    }
}
tasks {
    register("jarAndroid") {
        group = "build"
        dependsOn("jar")

        doLast {
            val sdkRoot = sdkRoot
            if (sdkRoot == null || !File(sdkRoot).exists()) throw GradleException("No valid Android SDK found. Ensure that ANDROID_HOME is set to your Android SDK directory.")
            val platformRoot = File("$sdkRoot/platforms/").listFiles()!!.sorted().reversed()
                .find { f -> File(f, "android.jar").exists() }
                ?: throw GradleException("No android.jar found. Ensure that you have an Android platform installed.")
            //collect dependencies needed for desugaring
            val allDependencies = configurations.compileClasspath.get().toList() +
                    configurations.runtimeClasspath.get().toList() +
                    listOf(File(platformRoot, "android.jar"))
            val dependencies = allDependencies.joinToString(" ") { "--classpath ${it.path}" }
            //dex and desugar files - this requires d8 in your PATH
            val paras = "$dependencies --min-api 14 --output ${archivesBaseName}Android.jar ${archivesBaseName}Desktop.jar"
            try {
                exec {
                    commandLine = "d8 $paras".split(' ')
                    workingDir = File("$buildDir/libs")
                    standardOutput = System.out
                    errorOutput = System.err
                }
            } catch (_: Exception) {
                val cmdOutput = ByteArrayOutputStream()
                logger.lifecycle("d8 cannot be found in your PATH, so trying to use an absolute path.")
                exec {
                    commandLine = listOf("where", "d8")
                    standardOutput = cmdOutput
                    errorOutput = System.err
                }
                val d8FullPath = cmdOutput.toString().replace("\r", "").replace("\n", "")
                exec {
                    commandLine = "$d8FullPath $paras".split(' ')
                    workingDir = File("$buildDir/libs")
                    standardOutput = System.out
                    errorOutput = System.err
                }
            }
        }
    }
    register("copyJar") {
        dependsOn("jar")
        doLast {
            val jarFile = getOutputJar() ?: return@doLast
            if (OperatingSystem.current().isWindows) {
                val APPDATA = System.getenv("APPDATA")
                val modsFolder = File("$APPDATA/Mindustry/mods")
                modsFolder.mkdirs()
                copyJarFile(jarFile, modsFolder)
            }
        }
    }
    register<DownloadTask>("downloadOpenGalPlumy") {
        group = "download"
        sourceUrl.set("https://github.com/liplum/OpenGalPlumy/releases/download/$plumyVersion/PlumyCompiler.jar")
        targetFile.set(File("$rootDir/run/PlumyCompiler$plumyVersion.jar"))
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
    register("copyJarServer") {
        dependsOn("jar")
        doLast {
            val jarFile = getOutputJar() ?: return@doLast
            val modsFolder = File("$rootDir/config/mods")
            copyJarFile(jarFile, modsFolder)
        }
    }
    register<Jar>("deploy") {
        group = "build"
        dependsOn("jarAndroid")
        dependsOn("jar")
        archiveFileName.set("${archivesBaseName}.jar")

        from(
            zipTree("$buildDir/libs/${archivesBaseName}Desktop.jar"),
            zipTree("$buildDir/libs/${archivesBaseName}Android.jar")
        )

        doLast {
            delete {
                delete("$buildDir/libs/${archivesBaseName}Desktop.jar")
                delete("$buildDir/libs/${archivesBaseName}Android.jar")
            }
        }
    }

    register<DownloadTask>("downloadDesktop") {
        group = "download"
        sourceUrl.set("http://github.com/Anuken/Mindustry/releases/download/${mdtVersion}/Mindustry.jar")
        targetFile.set(File("$rootDir/run/Mindustry${mdtVersionNum}.jar"))
        if (!targetFile.get().exists()) {
            logger.lifecycle("Downloading Mindustry ${mdtVersion}.")
        } else {
            logger.lifecycle("You have already downloaded this Mindustry.")
        }
    }

    register<DownloadTask>("downloadServer") {
        group = "download"
        sourceUrl.set("https://github.com/Anuken/Mindustry/releases/download/${mdtVersion}/server-release.jar")
        targetFile.set(File("$rootDir/run/MindustryServer${mdtVersionNum}.jar"))
        if (!targetFile.get().exists()) {
            logger.lifecycle("Downloading Mindustry Server ${mdtVersion}.")
        } else {
            logger.lifecycle("You have already downloaded this Mindustry Server.")
        }
    }


    register<JavaExec>("runMod") {
        group = "game"
        dependsOn("classes")
        dependsOn("jar")
        dependsOn("copyJar")
        dependsOn("downloadDesktop")
        val gameFile = File("$rootDir/run/Mindustry${mdtVersionNum}.jar")
        mainClass.set("-jar")
        args = listOf(gameFile.path)
    }

    register<JavaExec>("runGame") {
        group = "game"
        dependsOn("classes")
        dependsOn("downloadDesktop")
        val gameFile = File("$rootDir/run/Mindustry${mdtVersionNum}.jar")
        mainClass.set("-jar")
        args = listOf(gameFile.path)
    }

    register<JavaExec>("runModServer") {
        group = "game"
        dependsOn("classes")
        dependsOn("jar")
        dependsOn("copyJarServer")
        dependsOn("downloadServer")
        val gameFile = File("$rootDir/run/MindustryServer${mdtVersionNum}.jar")
        mainClass.set("-jar")
        standardInput = System.`in`
        args = listOf(gameFile.path)
    }

    register<JavaExec>("runServer") {
        group = "game"
        dependsOn("classes")
        dependsOn("downloadServer")
        val gameFile = File("$rootDir/run/MindustryServer${mdtVersionNum}.jar")
        mainClass.set("-jar")
        standardInput = System.`in`
        args = listOf(gameFile.path)
    }
}
tasks.withType<Test>().configureEach {
    useJUnitPlatform {
        includeTags("fast")
        excludeTags("slow")
    }
    testLogging {
        exceptionFormat = TestExceptionFormat.FULL
        showStandardStreams = true
    }
}
tasks.named<Jar>("jar") {
    dependsOn("compileGAL")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveFileName.set("${archivesBaseName}Desktop.jar")
    includeEmptyDirs = false

    from(
        configurations.runtimeClasspath.get().map {
            if (it.isDirectory) it else zipTree(it)
        }
    )

    from(rootDir) {
        // add something into your Jar
        include("mod.hjson")
        include("icon.png")
    }

    from("assets/") {
        include("**")
    }

    from("scripts/") {
        include("*.json")
    }
}
