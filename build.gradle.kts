import net.liplum.gradle.settings.Settings.localConfig

plugins{
    id("io.github.liplum.mgpp") version "1.1.6"
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
    version = "4.0"
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
}
mindustry {
    dependency {
        mindustry mirror "v136"
        arc on "v136.1"
    }
    client {
        mindustry official "v136.1"
    }
    server {
        mindustry official "v136.1"
    }
}
tasks {
    register("getReleaseHeader") {
        doLast {
            println("::set-output name=header::${rootProject.name} v$version on Mindustry v136")
            println("::set-output name=version::v$version")
        }
    }
}