plugins {
    kotlin("jvm")
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
    compileOnly("com.github.anuken.mindustryjitpack:core:$MindustryVersion")
    compileOnly("com.github.Anuken.Arc:arc-core:$ArcVersion")
}