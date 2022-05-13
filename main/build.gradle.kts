
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import java.io.ByteArrayOutputStream

plugins {
    kotlin("jvm") version "1.6.10"
    id("com.google.devtools.ksp") version "1.6.20-1.0.5"
    java
}
val outputJarName: String get() = extra["outputJarName"] as String
val mdtVersion: String get() = extra["mdtVersion"] as String
val mdtVersionNum: String get() = extra["mdtVersionNum"] as String
val sdkRoot: String? by extra(System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT"))
sourceSets {
    main {
        java.srcDir("src")
        resources.srcDir("resources")
    }
    test {
        java.srcDir("test")
        resources.srcDir("resources")
    }
}
/*
Ignore the default path of ksp generated files.
Replace it with copying generated codes into source path.
*/
kotlin.sourceSets.main {
    kotlin.srcDirs(
        file("$buildDir/generated/ksp/main/kotlin")
    )
}
/*
val properties = Properties()
properties.load(project.rootProject.file('local.properties').newDataInputStream())
*/
ksp {
    arg("PackageName", "net.liplum.gen")
    arg("FileName", "Contents")
    arg("GenerateSpec", "Contents")
    arg("Scope", "net.liplum.registries")
    allowSourcesFromOtherPlugins = true
    blockOtherCompilerPlugins = true
}
version = "4.0"
group = "net.liplum"
tasks.whenTaskAdded {
    if (name == "kspKotlin") {
           //enabled = false
    }
}
java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
val mdthash = "e856652db4"
dependencies {
    implementation(project(":annotations"))
    ksp(project(":processor"))
//    compileOnly("com.github.Anuken.Arc:arc-core:$mdtVersion")
//    compileOnly("com.github.Anuken.Mindustry:core:$mdtVersion")
    // Use anuke's mirror for now on https://github.com/Anuken/MindustryJitpack
    compileOnly("com.github.Anuken.Arc:arc-core:dfcb21ce56")
    //compileOnly(files("$rootDir/run/Mindustry136.jar"))
    compileOnly("com.github.anuken.mindustryjitpack:core:$mdthash")
    testImplementation("com.github.anuken.mindustryjitpack:core:$mdthash")
    implementation("com.github.liplum:OpenGAL:v0.4.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")

    testImplementation("com.github.Anuken.Arc:arc-core:dfcb21ce56")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
    testImplementation("com.github.liplum:TestUtils:v0.1")
    // annotationProcessor "com.github.Anuken:jabel:$jabelVersion"
}

tasks {
    register("jarAndroid") {
        group = "build"
        dependsOn("jar")

        doLast {
            val sdkRoot = sdkRoot
            if (sdkRoot == null || !File(sdkRoot).exists())
                throw GradleException("No valid Android SDK found. Ensure that ANDROID_HOME is set to your Android SDK directory.")
            val platformRoot = File("$sdkRoot/platforms/").listFiles()!!.sorted().reversed()
                .find { f -> File(f, "android.jar").exists() }
                ?: throw GradleException("No android.jar found. Ensure that you have an Android platform installed.")
            //collect dependencies needed for desugaring
            val allDependencies = configurations.compileClasspath.get().toList() +
                    configurations.runtimeClasspath.get().toList() +
                    listOf(File(platformRoot, "android.jar"))
            val dependencies = allDependencies.joinToString(" ") { "--classpath ${it.path}" }
            //dex and desugar files - this requires d8 in your PATH
            val paras = "$dependencies --min-api 14 --output ${outputJarName}Android.jar ${outputJarName}Desktop.jar"
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

    register<Jar>("deployLocal") {
        group = "build"
        dependsOn("jarAndroid")
        archiveFileName.set("${outputJarName}.jar")

        from(
            zipTree("$buildDir/libs/${outputJarName}Desktop.jar"),
            zipTree("$buildDir/libs/${outputJarName}Android.jar")
        )

        doLast {
            delete {
                delete("$buildDir/libs/${outputJarName}Android.jar")
            }
        }
    }

    register<Jar>("deploy") {
        group = "build"
        dependsOn("jarAndroid")
        archiveFileName.set("${outputJarName}.jar")

        from(
            zipTree("$buildDir/libs/${outputJarName}Desktop.jar"),
            zipTree("$buildDir/libs/${outputJarName}Android.jar")
        )

        doLast {
            delete {
                delete(
                    "$buildDir/libs/${outputJarName}Desktop.jar",
                    "$buildDir/libs/${outputJarName}Android.jar"
                )
            }
        }
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
    //dependsOn("compileGAL")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveFileName.set("${outputJarName}Desktop.jar")
    includeEmptyDirs = false
    exclude("**/**/*.java")
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

    from("$rootDir/assets") {
        include("**")
    }

    from("$rootDir/meta") {
        include("*.json")
    }
    from("$rootDir/extra") {
        include("**")
    }
}
