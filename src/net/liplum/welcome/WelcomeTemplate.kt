package net.liplum.welcome

import arc.scene.ui.Dialog
import mindustry.ui.dialogs.BaseDialog
import net.liplum.welcome.Welcome.Entity

abstract class WelcomeTemplate(
    val id: String,
) {
    abstract fun gen(entity: Entity): Dialog

    companion object {
        val Default = object : WelcomeTemplate("Default") {
            override fun gen(entity: Entity) =
                BaseDialog("Default").apply {
                    cont.add("Default")
                    addCloseButton()
                }
        }
    }
}