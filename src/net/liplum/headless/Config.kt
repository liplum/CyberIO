package net.liplum.headless

import arc.struct.ObjectMap
import arc.util.serialization.JsonValue
import mindustry.io.JsonIO
import net.liplum.FileSys
import net.liplum.utils.component1
import net.liplum.utils.component2
object Config {
    const val configName = "config.json"
    val Default = """
    {
        "AllowUpdate":false    
    }
    """.trimIndent()
    var pairs: Map<String, Any> = emptyMap()
    var metas: Map<String, Any> = mapOf(
        "AllowUpdate" to false,
    )

    fun load() {
    }
    @Suppress("UNCHECKED_CAST")
    fun tryLoad() {
        val config = FileSys.ConfigFolder.subF(configName).getOrCreate()
        val text = config.file.readText()
        val map = JsonIO.json.fromJson(ObjectMap::class.java, text) as ObjectMap<String, JsonValue>
        for ((name,v) in map){
            if(name in metas){
            }
        }
    }
}

