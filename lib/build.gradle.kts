plugins {
    kotlin("jvm")
    java
    `maven-publish`
}
val ArcVersion: String by project

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

dependencies {
    compileOnly("com.github.Anuken.Arc:arc-core:$ArcVersion")
    testImplementation("com.github.Anuken.Arc:arc-core:$ArcVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
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
            afterEvaluate {
                artifact(tasks.named("sourcesJar"))
            }
        }
    }
}