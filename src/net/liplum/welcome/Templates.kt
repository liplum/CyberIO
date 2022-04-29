package net.liplum.welcome

import arc.graphics.Texture
import arc.scene.ui.Button
import arc.scene.ui.Dialog
import arc.scene.ui.Label
import arc.scene.ui.TextButton
import arc.scene.ui.layout.Cell
import arc.scene.ui.layout.Table
import arc.scene.utils.Elem
import arc.util.Scaling
import mindustry.ui.dialogs.BaseDialog
import net.liplum.Meta
import net.liplum.update.Updater
import net.liplum.utils.TR
import net.liplum.welcome.TemplateRegistry.register
import net.liplum.welcome.Welcome.Entity

object Templates {
    private fun Table.addPoster(icon: TR) {
        icon.texture.setFilter(Texture.TextureFilter.nearest)
        image(icon).minSize(200f).scaling(Scaling.fill).row()
    }

    private fun Table.addCenterText(text: String) {
        add(Label(text).apply {
            setAlignment(0)
            setWrap(true)
            setFontScale(1.1f)
        }).growX()
            .row()
    }

    private inline fun Table.addCloseButton(
        dialog: Dialog,
        text: String,
        crossinline task: () -> Unit = {},
    ): Cell<TextButton> {
        return button(text) {
            Welcome.recordClick()
            task()
            dialog.hide()
        }.size(200f, 50f)
    }

    private inline fun Dialog.createCloseButton(
        text: String,
        crossinline task: () -> Unit = {},
    ): TextButton {
        return Elem.newButton(text) {
            Welcome.recordClick()
            task()
            hide()
        }
    }

    val Story = object : WelcomeTemplate("Story") {
        override fun gen(entity: Entity) =
            BaseDialog(entity["title"]).apply {
                cont.addPoster(entity.icon)
                cont.addCenterText(entity.bundle.format("welcome", Meta.DetailedVersion))
                cont.addCenterText(entity.content)
                cont.addCloseButton(this, entity["read"])
            }
    }.register()
    val YesNoDontShow = object : WelcomeTemplate("YesNoDontShow") {
        override fun gen(entity: Entity) =
            BaseDialog(entity["title"]).apply {
                val data = entity.tip.data
                val yesAction = ActionRegistry[data["YesAction"]]
                val noAction = ActionRegistry[data["NoAction"]]
                val dontShowAction = ActionRegistry[data["DontShowAction"]]
                cont.addPoster(entity.icon)
                cont.addCenterText(entity.content(Updater.latestVersion))
                cont.table {
                    it.addCloseButton(this, entity["yes"]) {
                        yesAction(entity)
                    }.size(150f, 50f)
                    it.addCloseButton(this, entity["no"]) {
                        noAction(entity)
                    }.size(150f, 50f)
                    it.addCloseButton(this, entity["dont-show"]) {
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