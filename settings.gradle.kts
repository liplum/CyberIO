rootProject.name = "cyberio"
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }

    plugins {
        kotlin("jvm") version "1.9.21"
    }
}
include(
    "main",
    "cui",
    "common",
    "processor",
    "annotations",
)
