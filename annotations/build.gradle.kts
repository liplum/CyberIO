import io.github.liplum.mindustry.importMindustry

plugins {
    kotlin("jvm")
    id("io.github.liplum.mgpp")
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
