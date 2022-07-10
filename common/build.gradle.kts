plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp") version "1.7.0-1.0.6"
    `maven-publish`
}
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

kotlin.sourceSets.main {
    kotlin.srcDirs(
        file("$buildDir/generated/ksp/main/kotlin"),
    )
}


dependencies {
    implementation(project(":annotations"))
    implementation(project(":lib"))
    ksp(project(":processor"))
    compileOnly("com.github.Anuken.Arc:arc-core:$ArcVersion")
    testImplementation("com.github.Anuken.Arc:arc-core:$ArcVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
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
            afterEvaluate {
                artifact(tasks.named("sourcesJar"))
            }
        }
    }
}