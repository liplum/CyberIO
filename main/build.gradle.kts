@file:Suppress("SpellCheckingInspection", "PropertyName")

import io.github.liplum.mindustry.antiAlias
import io.github.liplum.mindustry.importMindustry
import io.github.liplum.mindustry.mindustry
import io.github.liplum.mindustry.mindustryAssets
import net.liplum.gradle.mktxImplmenetation
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    `maven-publish`
    id("com.google.devtools.ksp")
    id("io.github.liplum.mgpp")
}
val PlumyVersion: String by project

sourceSets {
    main {
        java.srcDirs(
            "src",
            "${layout.buildDirectory}/generated/classGen",
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
        file("${layout.buildDirectory}/generated/ksp/main/kotlin"),
        file("${layout.buildDirectory}/generated/classGen"),
    )
}

ksp {
    arg("Event.FileName", "EventRegistry")
    arg("Event.GenerateSpec", "EventRegistry")
    arg("Dp.FileName", "Contents")
    arg("Dp.GenerateSpec", "Contents")
    arg("Dp.Scope", "net.liplum.registry")
    allowSourcesFromOtherPlugins = true
}
mindustry {
    run {
        clearOtherMods
    }
    deploy {
        baseName = "CyberIO"
        fatJar
    }
}
val antiAliasedDir = rootDir.resolve("assets-gen").resolve("sprites")
tasks.antiAlias {
    sourceDirectory.set(rootDir.resolve("assets-raw").resolve("sprites"))
    destinationDirectory.set(antiAliasedDir)
}
tasks.jar {
    dependsOn(tasks.antiAlias)
}
mindustryAssets {
    root at "$rootDir/assets"
    icon at "$rootDir/icon-raw.png"
    sprites {
        dependsOn(tasks.antiAlias)
        dir = antiAliasedDir
    }
}
tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += listOf(
        "-opt-in=kotlin.RequiresOptIn",
        "-Xcontext-receivers",
    )
}
val mktxVersion: String by project

dependencies {
    implementation(project(":annotations"))
    implementation(project(":common"))
    implementation(project(":cui"))
    ksp(project(":processor"))
    importMindustry("ksp")
    importMindustry()
    implementation("com.github.liplum.plumyjava:path-kt:$PlumyVersion")
    mktxImplmenetation(
        mktxVersion,
        "core",
        "texture",
        "world",
        "animation",
        "dsl"
    )
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
    testImplementation("com.github.liplum:TestUtils:v0.1")
}

tasks.jar {
    archiveBaseName.set("CyberIO")
    includeEmptyDirs = false
    exclude("**/**/*.java")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}