package net.liplum.gradle.settings

import com.beust.klaxon.Klaxon
import java.io.File

data class Config(
    val env: String = "prod",
)

object Settings {
    val json = Klaxon()
    @JvmStatic
    fun get(rootDir: File = File("")): Config {
        val settingsFile = File(rootDir, "local_settings.json")
        return if (!settingsFile.exists()) {
            val res = Config()
            if (settingsFile.createNewFile()) {
                settingsFile.writeText(json.toJsonString(res))
            }
            res
        } else {
            json.parse<Config>(settingsFile) ?: Config()
        }
    }
}