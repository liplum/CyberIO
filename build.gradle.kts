import io.github.liplum.mindustry.minGameVersion
import net.liplum.gradle.settings.Settings.localConfig
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") apply false
    id("com.google.devtools.ksp") version "1.9.21-1.0.15"
    id("io.github.liplum.mgpp") version "1.3.2"
}
buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://www.jitpack.io")
        }
    }
}
val settings = localConfig
allprojects {
    group = "net.liplum"
    version = "6.0"
    repositories {
        mavenCentral()
        maven {
            url = uri("https://www.jitpack.io")
        }
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform {
            excludeTags("slow")
        }
        testLogging {
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            showStandardStreams = true
        }
    }
    tasks.whenTaskAdded {
        tasks.whenTaskAdded {
            when (name) {
                "kspKotlin" -> if (settings.env == "dev") enabled = false
            }
        }
    }
    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
    tasks.withType<JavaCompile> {
        sourceCompatibility = JavaVersion.VERSION_1_8.toString()
        targetCompatibility = JavaVersion.VERSION_1_8.toString()
    }
}
mindustry {
    dependency {
        mindustry on "v146"
        arc on "v146"
    }
    client {
        /*mindustry from Foo(
            version = "v8.0.0",
            release = "erekir-client.jar"
        )*/
        mindustry official "v146"
    }
    server {
        mindustry official "v146"
    }
}

tasks.register("retrieveMeta") {
    doLast {
        println("::set-output name=header::Cyber IO v$version on Mindustry v${mindustry.meta.minGameVersion}")
        println("::set-output name=version::v$version")
        try {
            val releases = java.net.URL("https://api.github.com/repos/liplum/CyberIO/releases").readText()
            val gson = com.google.gson.Gson()
            val info = gson.fromJson<List<Map<String, Any>>>(releases, List::class.java)
            val tagExisted = info.any {
                it["tag_name"] == "v$version"
            }
            println("::set-output name=tag_exist::$tagExisted")
        } catch (e: Exception) {
            println("::set-output name=tag_exist::false")
            logger.warn("Can't fetch the releases", e)
        }
    }
}
