rootProject.name = "cyberio"
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }

    plugins {
        kotlin("jvm") version "1.8.0"
    }
}
include(
    "main",
    "cui",
    "common",
    "processor",
    "annotations",
)
