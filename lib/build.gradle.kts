import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    kotlin("jvm") version "1.6.10"
    id("com.google.devtools.ksp") version "1.6.20-1.0.5"
    java
}
val settings = net.liplum.gradle.settings.Settings.get(rootDir)

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

dependencies {
    implementation(project(":annotations"))
    ksp(project(":processor"))
    // Use anuke's mirror for now on https://github.com/Anuken/MindustryJitpack
    compileOnly("com.github.Anuken.Arc:arc-core:dfcb21ce56")
    testImplementation("com.github.Anuken.Arc:arc-core:dfcb21ce56")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
    testImplementation("com.github.liplum:TestUtils:v0.1")
    // annotationProcessor "com.github.Anuken:jabel:$jabelVersion"
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