package net.liplum

import arc.struct.ObjectMap
import arc.util.serialization.JsonValue
import mindustry.io.JsonIO
import net.liplum.lib.F
import net.liplum.lib.utils.component1
import net.liplum.lib.utils.component2
import net.liplum.mdt.HeadlessOnly

@HeadlessOnly
object Config {
    val Default = """
    {
        "AutoUpdate": false,
        "CheckUpdateInfoURL": null,
        "ContentSpecific": "vanilla"
    }
    """.trimIndent()
    var metas: Map<String, ConfigEntry<*>> = mapOf(
        "AutoUpdate" to ConfigEntry(false),
        "CheckUpdateInfoURL" to ConfigEntry(null, String::class.java),
        "ContentSpecific" to ConfigEntry(ContentSpec.Vanilla.id, String::class.java),
    )
    val AutoUpdate: Boolean
        get() = Config["AutoUpdate"] ?: false
    val CheckUpdateInfoURL: String
        get() = Config["CheckUpdateInfoURL"] ?: Meta.UpdateInfoURL
    val ContentSpecific: String
        get() = Config["ContentSpecific"] ?: ContentSpec.Vanilla.id
    const val configName = "config.json"
    var pairs: Map<String, Any?> = emptyMap()
    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(key: String): T? {
        if (key in pairs) {
            return pairs[key] as T?
        }
        if (key in metas) {
            return metas[key]?.default as T?
        }
        return null
    }
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
    @JvmStatic
    fun resetConfigFile() {
        configFile.delete().getOrCreate(Default) {
            CLog.info("${configFile.file.path} has created with initial config.")
        }
    }
    @Suppress("UNCHECKED_CAST")
    private fun tryLoad() {
        val config = configFile.getOrCreate(Default) {
            CLog.info("${configFile.file.path} has created with initial config.")
        }
        val text = config.file.readText()
        val map = JsonIO.json.fromJson(ObjectMap::class.java, text) as ObjectMap<String, Any?>
        val loaded = HashMap<String, Any?>()
        for ((name, v) in map) {
            val meta = metas[name]
            if (meta != null) {
                if (v == null) {
                    if (meta.allowNull)
                        loaded[name] = null
                    else
                        continue
                } else {
                    if (v is JsonValue)
                        loaded[name] = meta.convert(v)
                    else if (meta.matchClz(v))
                        loaded[name] = v
                }
            } else {
                CLog.info("Can't recognize $name=$v in config.")
            }
        }
        pairs = loaded
    }
}

class ConfigEntry<T : Any>(val default: T?, val clz: Class<T>, val allowNull: Boolean = true) {
    constructor(default: T) : this(default, clz = default.javaClass)
    @Suppress("UNCHECKED_CAST")
    fun convert(value: JsonValue): T {
        return when (clz) {
            Int::class.java -> value.asInt()
            String::class.java -> value.asString()
            Boolean::class.java -> value.asBoolean()
            else -> throw ClassCastException("No corresponding type as $clz")
        } as T
    }

    fun matchClz(value: Any) = clz.isInstance(value)
}

