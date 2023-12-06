package net.liplum.welcome

class WelcomeTip(
    @JvmField var id: String = DefaultID,
    @JvmField var conditionID: String = DefaultCondition,
    @JvmField var templateID: String = DefaultTemplateID,
    @JvmField var iconPath: String = DefaultIconPath,
    @JvmField var chance: Int = DefaultChance,
    @JvmField var data: Map<String, Any?> = emptyMap(),
) {
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
        const val DefaultIconPath = "icon"
    }
}

class WelcomeTipPack(
    var default: String = "Default",
    var scenes: List<String> = emptyList(),
) {
    operator fun get(index: Int) =
        if (index !in scenes.indices)
            default
        else
            scenes[index]

    fun indexOf(tipID: String): Int =
        scenes.indexOf(tipID)

    val defaultIndex: Int
        get() = indexOf(default)

    fun inherit(
        default: String? = null,
        scenes: List<String> = emptyList(),
    ): WelcomeTipPack {
        return WelcomeTipPack(
            default = default ?: this.default,
            scenes = this.scenes + scenes,
        )
    }
}