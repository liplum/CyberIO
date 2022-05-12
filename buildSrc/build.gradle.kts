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
    implementation("com.github.liplum:OpenGalPlumy:v0.1.4")
    implementation("com.google.devtools.ksp:symbol-processing-api:1.6.21-1.0.5")
}