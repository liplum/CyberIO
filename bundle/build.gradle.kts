plugins {
    kotlin("jvm") version "1.6.10"
    groovy
    java
}
repositories {
    mavenCentral()
    maven {
        url = uri("https://www.jitpack.io")
    }
}
sourceSets {
    main {
        java.srcDir("src")
    }
    test {
        java.srcDir("test")
    }
}

dependencies {
    implementation(gradleApi())
    implementation(project(":main","default"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform {
        includeTags("fast")
        excludeTags("slow")
    }
    testLogging {
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = true
    }
}