@file:Suppress("SpellCheckingInspection")

import net.liplum.gradle.gen.IConvertContext
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream

plugins {
    kotlin("jvm") version "1.6.10"
    id("com.google.devtools.ksp") version "1.6.20-1.0.5"
    java
}
val settings = net.liplum.gradle.settings.Settings.get(rootDir)
val OutputJarName: String by project
val MdtHash: String by project
val PlumyHash: String by project
val mdtVersion: String get() = extra["mdtVersion"] as String
val mdtVersionNum: String get() = extra["mdtVersionNum"] as String
val sdkRoot: String? by extra(System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT"))
sourceSets {
    main {
        java.srcDirs(
            "src",
            "${project(":mdt").projectDir}/src",
            "$buildDir/generated/classGen",
        )
        resources.srcDir("resources")
    }
    test {
        java.srcDir("test")
        resources.srcDir("resources")
    }
}

kotlin.sourceSets.main {
    kotlin.srcDirs(
        file("$buildDir/generated/ksp/main/kotlin"),
        file("${project(":mdt").projectDir}/src"),
        file("$buildDir/generated/classGen"),
    )
}

ksp {
    arg("Event.FileName", "EventRegistry")
    arg("Event.GenerateSpec", "EventRegistry")
    arg("Dp.FileName", "Contents")
    arg("Dp.GenerateSpec", "Contents")
    arg("Dp.Scope", "net.liplum.registries")
    allowSourcesFromOtherPlugins = true
    blockOtherCompilerPlugins = true
}
version = "4.0"
group = "net.liplum"
tasks.whenTaskAdded {
    if (name == "kspKotlin") {
        if (settings.env == "dev") {
            enabled = false
        }
    }
}
java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
}
dependencies {
    implementation(project(":annotations"))
    implementation(project(":lib"))
    ksp(project(":processor"))
    ksp("com.github.anuken.mindustryjitpack:core:$MdtHash")
//    compileOnly("com.github.Anuken.Arc:arc-core:$mdtVersion")
//    compileOnly("com.github.Anuken.Mindustry:core:$mdtVersion")
    // Use anuke's mirror for now on https://github.com/Anuken/MindustryJitpack
    compileOnly("com.github.Anuken.Arc:arc-core:dfcb21ce56")
    //compileOnly(files("$rootDir/run/Mindustry136.jar"))
    compileOnly("com.github.anuken.mindustryjitpack:core:$MdtHash")
    testImplementation("com.github.anuken.mindustryjitpack:core:$MdtHash")
    implementation("com.github.liplum:OpenGAL:v0.4.3")
    implementation("com.github.liplum.plumyjava:path-kt:$PlumyHash")

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
            val paras = "$dependencies --min-api 14 --output ${OutputJarName}Android.jar ${OutputJarName}Desktop.jar"
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
        archiveFileName.set("${OutputJarName}.jar")

        from(
            zipTree("$buildDir/libs/${OutputJarName}Desktop.jar"),
            zipTree("$buildDir/libs/${OutputJarName}Android.jar")
        )

        doLast {
            delete {
                delete("$buildDir/libs/${OutputJarName}Android.jar")
            }
        }
    }

    register<Jar>("deploy") {
        group = "build"
        dependsOn("jarAndroid")
        archiveFileName.set("${OutputJarName}.jar")

        from(
            zipTree("$buildDir/libs/${OutputJarName}Desktop.jar"),
            zipTree("$buildDir/libs/${OutputJarName}Android.jar")
        )

        doLast {
            delete {
                delete(
                    "$buildDir/libs/${OutputJarName}Desktop.jar",
                    "$buildDir/libs/${OutputJarName}Android.jar"
                )
            }
        }
    }
    register<net.liplum.gradle.tasks.GenerateStaticClassTask>("genMetaClass") {
        group = "build"
        jsonPath.set("$rootDir/meta/Meta.json")
        args.set(
            mapOf(
                "Condition" to settings.env
            )
        )
        converters.set(
            mapOf(
                "Version2" to object : net.liplum.gradle.gen.ClassConverter("net.liplum.update.Version2") {
                    override fun convert(context: IConvertContext, value: Any): String =
                        context.newObject(qualifiedClassName, *(value as String).split(".").toTypedArray())
                }
            )
        )
    }
    named("compileJava") {
        dependsOn("genMetaClass")
    }
    named("compileKotlin") {
        dependsOn("genMetaClass")
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
    archiveFileName.set("${OutputJarName}Desktop.jar")
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
