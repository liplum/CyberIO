import io.github.liplum.mindustry.importMindustry

plugins {
    kotlin("jvm")
    id("io.github.liplum.mgpp")
}
java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
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
    importMindustry()
}