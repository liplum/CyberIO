package net.liplum.ui

import arc.scene.ui.ScrollPane
import arc.scene.ui.TextButton
import arc.scene.ui.layout.Table
import mindustry.gen.Iconc
import mindustry.gen.Sounds
import mindustry.gen.Tex
import mindustry.ui.dialogs.BaseDialog
import net.liplum.R
import net.liplum.Var
import net.liplum.lib.bundle

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
    fun show() {
        BaseDialog(bundle("title")).apply {
            addCloseButton()
            cont.add(ScrollPane(Table(Tex.button).apply {
                add(TextButton(enableButtonText).apply {
                    isDisabled = Var.EnableMapCleaner
                    clicked {
                        Var.EnableMapCleaner = true
                        isDisabled = Var.EnableMapCleaner
                        Sounds.message.play()
                        setText(enableButtonText)
                    }
                }).pad(5f).fillX().row()
                add("${R.Advanced.MapCleaner}.description".bundle).wrap().width(500f).pad(5f)
            }))
        }.show()
    }
}