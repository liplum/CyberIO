package net.liplum.welcome

import arc.scene.ui.Button
import mindustry.ui.dialogs.BaseDialog
import net.liplum.Meta
import net.liplum.update.Updater
import net.liplum.welcome.Welcome.Entity

object Templates {
    val Story = object : WelcomeTemplate("Story") {
        override fun gen(entity: Entity) =
            BaseDialog(entity["title"]).apply {
                addPoster(entity.icon)
                addCenterText(entity.bundle.format("welcome", Meta.DetailedVersion))
                addCenterText(entity.content)
                addCloseButton(entity["read"])
            }
    }
    val ButtonABC = object : WelcomeTemplate("ButtonABC") {
        override fun gen(entity: Entity) =
            BaseDialog(entity["title"]).apply {
                val data = entity.tip.data
                val yesAction = ActionRegistry[data["ActionA"]]
                val noAction = ActionRegistry[data["ActionB"]]
                val dontShowAction = ActionRegistry[data["ActionC"]]
                addPoster(entity.icon)
                addCenterText(entity.content(Updater.latestVersion))
                cont.table {
                    addCloseButton(entity["button-a"], it) {
                        yesAction(entity)
                    }.size(150f, 50f)
                    addCloseButton(entity["button-b"], it) {
                        noAction(entity)
                    }.size(150f, 50f)
                    addCloseButton(entity["button-c"], it) {
                        dontShowAction(entity)
                    }.size(150f, 50f)
                }.growX()
                    .row()
            }
    }
    val DoAction = object : WelcomeTemplate("DoAction") {
        override fun gen(entity: Entity) =
            BaseDialog(entity["title"]).apply {
                val data = entity.tip.data
                val yesAction = ActionRegistry[data["YesAction"]]
                val noAction = ActionRegistry[data["NoAction"]]
                addPoster(entity.icon)
                addCenterText(entity.content)
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
    }
}