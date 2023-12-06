package net.liplum.welcome

class WelcomeScene(
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
        val Default = WelcomeScene()
        const val DefaultID = "Default"
        const val DefaultCondition = "ShowWelcome"
        const val DefaultTemplateID = "Story"
        const val DefaultChance = 1000
        const val DefaultIconPath = "icon"
    }
}

class WelcomeScenePack(
    var default: WelcomeScene,
    var scenes: List<WelcomeScene> = emptyList(),
) {
    operator fun get(index: Int): WelcomeScene =
        if (index !in scenes.indices)
            default
        else
            scenes[index]

    fun indexOf(tipID: WelcomeScene): Int =
        scenes.indexOf(tipID)

    fun inherit(
        default: WelcomeScene? = null,
        scenes: List<WelcomeScene> = emptyList(),
    ): WelcomeScenePack {
        return WelcomeScenePack(
            default = default ?: this.default,
            scenes = this.scenes + scenes,
        )
    }
}