package net.liplum

import arc.util.Log
import arc.util.serialization.Json
import arc.util.serialization.JsonWriter
import net.liplum.common.F
import plumy.core.HeadlessOnly

@HeadlessOnly
class ConfigEntry private constructor() {
    var AutoUpdate: Boolean = false
    var ContentSpecific: String = "vanilla"

    companion object {
        lateinit var Config: ConfigEntry
        var json = Json(JsonWriter.OutputType.json)
        const val configName = "config.json"
        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        fun load() {
            runCatching {
                tryLoad()
            }.onFailure {
                CLog.err(
                    "Can't load config because ${it.message}. Please check the format at ${configFile.file.path}. Or you can delete it directly and it will be regenerated next time starting up.",
                    it
                )
            }
        }

        val configFile: F
            get() = FileSys.CyberIoFolder.subF(configName)
        val emptyConfigContent: String by lazy { json.toJson(ConfigEntry()) }
        @JvmStatic
        fun resetConfigFile() {
            configFile.overwrite(emptyConfigContent)
            CLog.info("${configFile.file.path} has created with initial config.")
        }

        private fun tryLoad() {
            val configFile = configFile.getOrCreate({ emptyConfigContent }) {
                CLog.info("${configFile.file.path} has created with initial config.")
            }
            val text = configFile.file.readText()
            Config = try {
                json.fromJson(ConfigEntry::class.java, text)
            } catch (e: Exception) {
                Log.err(e)
                ConfigEntry()
            }
        }
    }

    fun trySave() {
        val json = json.toJson(this)
        configFile.file.writeText(json)
    }
}
