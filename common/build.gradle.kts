import io.github.liplum.mindustry.importMindustry
plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp") version "1.7.0-1.0.6"
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
val MKUtilsVersion :String by project

dependencies {
    api(project(":annotations"))
    testApi(project(":annotations"))
    ksp(project(":processor"))
    importMindustry()
    api("com.github.plumygame.mkutils:core:$MKUtilsVersion")
    testApi("com.github.plumygame.mkutils:core:$MKUtilsVersion")
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