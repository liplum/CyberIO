package net.liplum.welcome

import arc.struct.Seq
import arc.util.serialization.JsonValue
import mindustry.io.JsonIO
import net.liplum.lib.Res

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
            val iconPath = entry.get("IconPath").asString()
            val template = entry.get("Template").asString()
            all[id] = WelcomeTip().apply {
                this.id = id
                this.iconPath = iconPath
                this.templateID = template
            }
        }
        list = all
    }

    operator fun get(id: String) = list[id] ?: WelcomeTip.Default
}

class WelcomeTip {
    @JvmField var id: String = "Default"
    @JvmField var templateID: String = "Default"
    @JvmField var iconPath: String = "welcome-cyber-io"
    override fun toString() = id
    val template: WelcomeTemplate
        get() = TemplateRegistry[templateID]

    companion object {
        val Default = WelcomeTip()
    }
}
