import io.github.liplum.mindustry.importMindustry
plugins {
    kotlin("jvm")
    id("io.github.liplum.mgpp")
}
val MindustryVersion: String by project
val ArcVersion: String by project
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