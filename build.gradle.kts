buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://www.jitpack.io")
        }
    }
}
val settings = net.liplum.gradle.settings.Settings.get(rootDir)
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
tasks {
    register("test") {
        group = "verification"
        doLast {
            allprojects.forEach {
                it.tasks.withType<Test>().forEach { test ->
                    test.executeTests()
                }
            }
        }
    }
}