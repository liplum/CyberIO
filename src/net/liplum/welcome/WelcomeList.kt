package net.liplum.welcome

import arc.struct.Seq
import arc.util.serialization.JsonValue
import mindustry.io.JsonIO
import net.liplum.lib.Res
import net.liplum.utils.getValue

object WelcomeList {
    var list: Map<String, WelcomeTip> = emptyMap()
    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun loadList() {
        val json = Res("Welcomes.json").readAllText()
        val array = JsonIO.json.fromJson(Seq::class.java, json) as Seq<JsonValue>
        val all = HashMap<String, WelcomeTip>()
        for (entry in array) {
            val id = entry.get("ID").asString()
            val iconPath: String? = entry.get("IconPath")?.asString()
            val template: String? = entry.get("Template")?.asString()
            val data: JsonValue? = entry.get("Data")
            all[id] = WelcomeTip().apply {
                this.id = id
                iconPath?.let { this.iconPath = it }
                template?.let { this.templateID = it }
                data?.let { j ->
                    this.data = j.associate { it.name to it.getValue() }
                }
            }
        }
        list = all
    }

    operator fun get(id: String) = list[id] ?: WelcomeTip.Default
}

class WelcomeTip {
    @JvmField var id: String = DefaultID
    @JvmField var templateID: String = DefaultTemplateID
    @JvmField var iconPath: String = DefaultIconPath
    @JvmField var data: Map<String, Any?> = emptyMap()
    override fun toString() = id
    val template: WelcomeTemplate
        get() = TemplateRegistry[templateID]

    companion object {
        val Default = WelcomeTip()
        const val DefaultID = "Default"
        const val DefaultTemplateID = "Story"
        const val DefaultIconPath = "welcome-cyber-io"
    }
}
