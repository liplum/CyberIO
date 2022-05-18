package net.liplum.welcome

import arc.struct.Seq
import arc.util.serialization.JsonValue
import mindustry.io.JsonIO
import net.liplum.lib.Res
import net.liplum.lib.utils.getValue

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
            val condition: String? = entry.get("Condition")?.asString()
            val chance: Int? = entry.get("Chance")?.asInt()
            val data: JsonValue? = entry.get("Data")
            all[id] = WelcomeTip().apply {
                this.id = id
                iconPath?.let { this.iconPath = it }
                template?.let { this.templateID = it }
                data?.let { j ->
                    this.data = j.associate { it.name to it.getValue() }
                }
                condition?.let { this.conditionID = it }
                chance?.let { this.chance = it }
            }
        }
        list = all
    }

    operator fun get(id: String) = list[id] ?: WelcomeTip.Default
    inline fun find(filter: (WelcomeTip) -> Boolean): WelcomeTip? {
        return list.values.find(filter)
    }
    inline fun findAll(filter: (WelcomeTip) -> Boolean): List<WelcomeTip> {
        return list.values.filter(filter)
    }
}

class WelcomeTip {
    @JvmField var id: String = DefaultID
    @JvmField var conditionID: String = DefaultCondition
    @JvmField var templateID: String = DefaultTemplateID
    @JvmField var iconPath: String = DefaultIconPath
    @JvmField var chance: Int = DefaultChance
    @JvmField var data: Map<String, Any?> = emptyMap()
    override fun toString() = id
    val template: WelcomeTemplate
        get() = TemplateRegistry[templateID]
    val condition: Condition
        get() = ConditionRegistry[conditionID]

    companion object {
        val Default = WelcomeTip()
        const val DefaultID = "Default"
        const val DefaultCondition = "ShowWelcome"
        const val DefaultTemplateID = "Story"
        const val DefaultChance = 1000
        const val DefaultIconPath = "welcome-cyber-io"
    }
}
