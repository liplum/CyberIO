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
val MdtHash: String by project

dependencies {
    implementation(project(":annotations"))
    // compileOnly("com.github.Anuken.Arc:arc-core:$mdtVersion")
    // compileOnly("com.github.Anuken.Mindustry:core:$mdtVersion")
    // Use anuke's mirror for now on https://github.com/Anuken/MindustryJitpack
    compileOnly("com.github.Anuken.Arc:arc-core:dfcb21ce56")
    compileOnly("com.github.anuken.mindustryjitpack:core:$MdtHash")
    implementation("com.google.guava:guava:31.1-jre")
    implementation("com.google.devtools.ksp:symbol-processing-api:1.6.21-1.0.5")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.4.8")
    testImplementation ("com.github.tschuchortdev:kotlin-compile-testing-ksp:1.4.8")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.21")
    testImplementation("org.jetbrains.kotlin:kotlin-reflect:1.6.21")
}
