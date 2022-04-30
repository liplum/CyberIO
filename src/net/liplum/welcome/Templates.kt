package net.liplum.welcome

import arc.scene.ui.Button
import mindustry.ui.dialogs.BaseDialog
import net.liplum.Meta
import net.liplum.update.Updater
import net.liplum.welcome.TemplateRegistry.register
import net.liplum.welcome.Welcome.Entity

object Templates {
    val Story = object : WelcomeTemplate("Story") {
        override fun gen(entity: Entity) =
            BaseDialog(entity["title"]).apply {
                cont.addPoster(entity.icon)
                cont.addCenterText(entity.bundle.format("welcome", Meta.DetailedVersion))
                cont.addCenterText(entity.content)
                cont.addCloseButton(this, entity["read"])
            }
    }.register()
    val ButtonABC = object : WelcomeTemplate("ButtonABC") {
        override fun gen(entity: Entity) =
            BaseDialog(entity["title"]).apply {
                val data = entity.tip.data
                val yesAction = ActionRegistry[data["ActionA"]]
                val noAction = ActionRegistry[data["ActionB"]]
                val dontShowAction = ActionRegistry[data["ActionC"]]
                cont.addPoster(entity.icon)
                cont.addCenterText(entity.content(Updater.latestVersion))
                cont.table {
                    it.addCloseButton(this, entity["button-a"]) {
                        yesAction(entity)
                    }.size(150f, 50f)
                    it.addCloseButton(this, entity["button-b"]) {
                        noAction(entity)
                    }.size(150f, 50f)
                    it.addCloseButton(this, entity["button-c"]) {
                        dontShowAction(entity)
                    }.size(150f, 50f)
                }.growX()
                    .row()
            }
    }.register()
    val DoAction = object : WelcomeTemplate("DoAction") {
        override fun gen(entity: Entity) =
            BaseDialog(entity["title"]).apply {
                val data = entity.tip.data
                val yesAction = ActionRegistry[data["YesAction"]]
                val noAction = ActionRegistry[data["NoAction"]]
                cont.addPoster(entity.icon)
                cont.addCenterText(entity.content)
                val order = data["Order"] as? String ?: "YesNo"
                cont.table {
                    fun addButton(vararg buttons: Button) {
                        for (b in buttons)
                            it.add(b).size(200f, 50f)
                    }

                    val yes = createCloseButton(entity["yes"]) {
                        yesAction(entity)
                    }
                    val no = createCloseButton(entity["no"]) {
                        noAction(entity)
                    }
                    if (order == "NoYes")
                        addButton(no, yes)
                    else
                        addButton(yes, no)
                }.growX()
                    .row()
            }
    }.register()
}