buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://www.jitpack.io")
        }
    }
}
allprojects {
    group = "net.liplum"
    version = "4.0"
    val mdtVersion by extra(property("MindustryVersion") as String)
    extra["outputJarName"] = property("OutputJarName") as String
    extra["mdtVersion"] = mdtVersion
    extra["mdtVersionNum"] = mdtVersion.replace("v", "")
    repositories {
        mavenCentral()
        maven {
            url = uri("https://www.jitpack.io")
        }
    }
}