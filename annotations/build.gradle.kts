plugins {
    kotlin("jvm") version "1.6.10"
}
sourceSets {
    main {
        java.srcDir("src")
    }
    test {
        java.srcDir("test")
    }
}
val MdtHash: String by project
dependencies {
    // compileOnly("com.github.Anuken.Arc:arc-core:$mdtVersion")
    // compileOnly("com.github.Anuken.Mindustry:core:$mdtVersion")
    // Use anuke's mirror for now on https://github.com/Anuken/MindustryJitpack
    compileOnly("com.github.Anuken.Arc:arc-core:dfcb21ce56")
    compileOnly("com.github.anuken.mindustryjitpack:core:$MdtHash")
}