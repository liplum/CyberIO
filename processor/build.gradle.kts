import io.github.liplum.mindustry.importMindustry

plugins {
    kotlin("jvm")
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
dependencies {
    implementation(project(":annotations"))
    importMindustry()
    implementation("com.google.guava:guava:31.1-jre")
    implementation("com.google.devtools.ksp:symbol-processing-api:1.9.21-1.0.15")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.5.0")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing-ksp:1.5.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.21")
    testImplementation("org.jetbrains.kotlin:kotlin-reflect:1.9.21")
}
