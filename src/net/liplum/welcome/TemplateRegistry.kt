package net.liplum.welcome

import arc.scene.ui.Dialog
import mindustry.ui.dialogs.BaseDialog
import net.liplum.welcome.TemplateRegistry.register

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
}

abstract class WelcomeTemplate(
    val id: String,
) {
    init {
        this.register()
    }

    abstract fun gen(entity: Welcome.Entity): Dialog

    companion object {
        val Default = object : WelcomeTemplate("Default") {
            override fun gen(entity: Welcome.Entity) =
                BaseDialog(entity.bundle["Default.title"]).apply {
                    cont.add(entity.bundle["Default"])
                    addCloseButton()
                }
        }
    }
}
