package net.liplum.welcome

object TemplateRegistry {
    val templates: MutableMap<String, WelcomeTemplate> = HashMap()
    operator fun get(id: String) =
        templates[id] ?: WelcomeTemplate.Default

    operator fun set(id: String, template: WelcomeTemplate) {
        templates[id] = template
    }

    fun <T : WelcomeTemplate> T.register(): T {
        this@TemplateRegistry[id] = this
        return this
    }

    fun load() {}
}