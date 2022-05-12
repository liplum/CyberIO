plugins {
    kotlin("jvm") version "1.6.10"
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
// Versions are declared in 'gradle.properties' file

dependencies {
    implementation(project(":annotations"))
    implementation("com.google.devtools.ksp:symbol-processing-api:1.6.21-1.0.5")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.4.8")
    testImplementation ("com.github.tschuchortdev:kotlin-compile-testing-ksp:1.4.8")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.21")
    testImplementation("org.jetbrains.kotlin:kotlin-reflect:1.6.21")
}
tasks.withType<Test>().configureEach {
    useJUnitPlatform {
    }
    testLogging {
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = true
    }
}