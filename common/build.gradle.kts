import io.github.liplum.mindustry.importMindustry
import net.liplum.gradle.mktxApi

plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp") version "1.8.0-1.0.8"
    `maven-publish`
    id("io.github.liplum.mgpp")
}

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

kotlin.sourceSets.main {
    kotlin.srcDirs(
        file("$buildDir/generated/ksp/main/kotlin"),
    )
}
val mktxVersion: String by project

dependencies {
    api(project(":annotations"))
    testApi(project(":annotations"))
    ksp(project(":processor"))
    importMindustry()
    mktxApi(
        mktxVersion,
        "core",
        "dsl",
    )
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
    testImplementation("com.github.liplum:TestUtils:v0.1")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}