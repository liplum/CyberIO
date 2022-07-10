rootProject.name = "cyberio"
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }

    plugins {
        kotlin("jvm").version(extra["KotlinVersion"] as String)
    }
}
include(
    "main",
    "lib",
    "cui",
    "mdt",
    "common",
    "story",
    "processor",
    "annotations",
)
