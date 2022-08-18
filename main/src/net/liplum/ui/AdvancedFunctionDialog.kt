package net.liplum.ui

import arc.scene.ui.Button
import arc.scene.ui.Label
import arc.scene.ui.ScrollPane
import arc.scene.ui.TextButton
import arc.scene.ui.layout.Cell
import arc.scene.ui.layout.Table
import mindustry.gen.Iconc
import mindustry.gen.Sounds
import mindustry.gen.Tex
import mindustry.ui.dialogs.BaseDialog
import net.liplum.R
import net.liplum.Var
import net.liplum.common.delegate.Delegate
import net.liplum.common.util.IBundlable
import plumy.dsl.bundle

object AdvancedFunctionDialog : IBundlable {
    override val bundlePrefix = "setting.${R.Gen("advanced-function")}"
    @JvmStatic
    fun show(onReset: Delegate) {
        BaseDialog(bundle("title")).apply {
            addCloseButton()
            cont.add(ScrollPane(Table().apply {
                // Map Cleaner
                addFunction {
                    fun enableButtonText() =
                        if (!Var.EnableMapCleaner) R.Ctrl.Enable.bundle(R.Advanced.MapCleaner.bundle)
                        else "${R.Advanced.MapCleaner.bundle} ${Iconc.ok}"
                    add(TextButton(enableButtonText()).apply {
                        isDisabled = Var.EnableMapCleaner
                        fun updateButtonState() {
                            isDisabled = Var.EnableMapCleaner
                            Sounds.message.play()
                            setText(enableButtonText())
                        }
                        changed {
                            Var.EnableMapCleaner = true
                            updateButtonState()
                        }
                        onReset += {
                            Var.EnableMapCleaner = false
                            updateButtonState()
                        }
                    }).applyButtonStyle().row()
                    add("${R.Advanced.MapCleaner}.description".bundle).applyLabelStyle()
                }
            }))
        }.show()
    }

    inline fun Table.addFunction(func: Table.() -> Unit): Cell<Table> =
        add(Table(Tex.button).apply(func)).apply { row() }

    fun <T : Button> Cell<T>.applyButtonStyle() = apply {
        pad(5f).fillX()
    }

    fun <T : Label> Cell<T>.applyLabelStyle() = apply {
        wrap().width(500f).pad(5f)
    }
}