package net.liplum

import arc.struct.ObjectMap
import arc.util.Log
import arc.util.serialization.JsonValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import mindustry.io.JsonIO
import net.liplum.scripts.KeyNotFoundException
import net.liplum.utils.component1
import net.liplum.utils.component2
import kotlin.coroutines.CoroutineContext

object Config : CoroutineScope {
    val Default = """
    {
        "AutoUpdate": false
    }
    """.trimIndent()
    var metas: Map<String, ConfigEntry<*>> = mapOf(
        "AutoUpdate" to ConfigEntry(false),
    )
    val AutoUpdate: Boolean
        get() = Config["AutoUpdate"]
    const val configName = "config.json"
    var pairs: Map<String, Any> = emptyMap()
    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(key: String): T {
        val inPairs = pairs[key]
        if (inPairs != null)
            return inPairs as T
        val inMetas = metas[key]?.default
        if (inMetas != null)
            return inMetas as T
        throw KeyNotFoundException(key)
    }
    @JvmStatic
    fun load() {
        loadConfigJob = launch {
            runCatching {
                tryLoad()
            }.onFailure {
                Log.err("Can't load configuration of CyberIO.", it)
            }
        }
    }
    @Suppress("UNCHECKED_CAST")
    private fun tryLoad() {
        val config = FileSys.CyberIoFolder.subF(configName).getOrCreate(Default)
        val text = config.file.readText()
        val map = JsonIO.json.fromJson(ObjectMap::class.java, text) as ObjectMap<String, Any>
        val loaded = HashMap<String, Any>()
        for ((name, v) in map) {
            metas[name]?.let {
                if (v is JsonValue)
                    loaded[name] = it.convert(v)
                else if (it.matchClz(v))
                    loaded[name] = v
            }
        }
        pairs = loaded
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO
    var loadConfigJob: Job? = null
}

class ConfigEntry<T : Any>(val default: T, val clz: Class<T>) {
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

