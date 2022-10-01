import io.github.liplum.mindustry.*

plugins {
    kotlin("jvm")
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

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
val MKUtilsVersion: String by project

dependencies {
    api(project(":annotations"))
    api(project(":cui"))
    api(project(":common"))
    testApi(project(":annotations"))
    testApi(project(":cui"))
    testApi(project(":common"))
    importMindustry()
    // mkutils core
    api("com.github.plumygame.mkutils:core:$MKUtilsVersion")
    testApi("com.github.plumygame.mkutils:core:$MKUtilsVersion")
    // mkutils world
    api("com.github.plumygame.mkutils:world:$MKUtilsVersion")
    testApi("com.github.plumygame.mkutils:world:$MKUtilsVersion")
    // mkutils animation
    api("com.github.plumygame.mkutils:animation:$MKUtilsVersion")
    testApi("com.github.plumygame.mkutils:animation:$MKUtilsVersion")
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