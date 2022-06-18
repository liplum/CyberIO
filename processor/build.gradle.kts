plugins {
    kotlin("jvm")
}
val MindustryVersion: String by project
val ArcVersion: String by project
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
    compileOnly("com.github.anuken.mindustryjitpack:core:$MindustryVersion")
    compileOnly("com.github.Anuken.Arc:arc-core:$ArcVersion")
    implementation("com.google.guava:guava:31.1-jre")
    implementation("com.google.devtools.ksp:symbol-processing-api:1.6.21-1.0.5")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.4.9")
    testImplementation ("com.github.tschuchortdev:kotlin-compile-testing-ksp:1.4.9")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.21")
    testImplementation("org.jetbrains.kotlin:kotlin-reflect:1.6.21")
}
