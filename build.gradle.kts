buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://www.jitpack.io")
        }
    }
}
val settings = net.liplum.gradle.settings.Settings.get(rootDir)
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
    tasks.whenTaskAdded {
        tasks.whenTaskAdded {
            when (name) {
                "kspKotlin" -> if (settings.env == "dev") enabled = false
            }
        }
    }
}