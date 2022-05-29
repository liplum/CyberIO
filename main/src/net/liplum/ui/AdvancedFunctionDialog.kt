package net.liplum.ui

import arc.scene.ui.Button
import arc.scene.ui.Label
import arc.scene.ui.ScrollPane
import arc.scene.ui.TextButton
import arc.scene.ui.layout.Cell
import arc.scene.ui.layout.Table
import arc.scene.utils.Elem
import mindustry.gen.Iconc
import mindustry.gen.Sounds
import mindustry.gen.Tex
import mindustry.ui.dialogs.BaseDialog
import net.liplum.R
import net.liplum.Var
import net.liplum.lib.delegates.Delegate
import net.liplum.lib.utils.bundle

object AdvancedFunctionDialog {
    val prefix = "setting.${R.Gen("advanced-function")}"
    @JvmStatic
    fun bundle(key: String, vararg args: Any) =
        if (args.isEmpty()) "$prefix.$key".bundle
        else "$prefix.$key".bundle(*args)

    val enableButtonText: String
        get() = if (!Var.EnableMapCleaner) R.Ctrl.Enable.bundle(R.Advanced.MapCleaner.bundle)
        else "${R.Advanced.MapCleaner.bundle} ${Iconc.ok}"
    @JvmStatic
    fun show(onReset: Delegate) {
        BaseDialog(bundle("title")).apply {
            addCloseButton()
            // GitHub Mirror
            add(Table(Tex.button).apply {
                add(Elem.newButton(GitHubMirrorUrlDialog.bundle("button")) {
                    GitHubMirrorUrlDialog.show(onReset)
                }).applyButtonStyle().row()
                add(GitHubMirrorUrlDialog.bundle("button-tooltip")).applyLabelStyle()
            }).row()
            // Map Cleaner
            cont.add(ScrollPane(Table().apply {
                add(Table(Tex.button).apply {
                    add(TextButton(enableButtonText).apply {
                        isDisabled = Var.EnableMapCleaner
                        fun updateButtonState() {
                            isDisabled = Var.EnableMapCleaner
                            Sounds.message.play()
                            setText(enableButtonText)
                        }
                        clicked {
                            Var.EnableMapCleaner = true
                            updateButtonState()
                        }
                        onReset += {
                            Var.EnableMapCleaner = false
                            updateButtonState()
                        }
                    }).applyButtonStyle().row()
                    add("${R.Advanced.MapCleaner}.description".bundle).applyLabelStyle()
                })
            }))
        }.show()
    }

    fun <T : Button> Cell<T>.applyButtonStyle() = apply {
        pad(5f).fillX()
    }

    fun <T : Label> Cell<T>.applyLabelStyle() = apply {
        wrap().width(500f).pad(5f)
    }
}