plugins {
    kotlin("jvm") version "1.6.10"
    groovy
    java
}
buildscript{
    dependencies{
        classpath(kotlin("gradle-plugin", version = "1.6.10"))
    }
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
    implementation("com.beust:klaxon:5.5")
    implementation("com.github.liplum:OpenGalPlumy:v0.1.4")
}