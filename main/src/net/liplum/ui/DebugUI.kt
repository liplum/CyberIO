package net.liplum.ui

import arc.Core
import arc.scene.event.Touchable
import arc.scene.ui.layout.Table
import arc.scene.ui.layout.WidgetGroup
import mindustry.Vars
import mindustry.gen.Tex
import net.liplum.Debug
import net.liplum.R
import net.liplum.inputs.Screen
import net.liplum.lib.utils.bundle
import net.liplum.mdt.lock
import net.liplum.mdt.ui.DatabaseSelectorDialog
import net.liplum.mdt.ui.NewBaseDialog
import net.liplum.mdt.utils.ForEachUnlockableContent

object DebugUI {
    @JvmStatic
    fun appendUI() {
        addMousePosition()
    }

    fun addMousePosition() {
        val hudGroup = Vars.ui.hudGroup
        val minimap = hudGroup.find<Table>("minimap/position")
        minimap?.apply {
            row()
            label {
                val build = Screen.tileOnMouse()?.build
                if (build != null)
                    "Build:${build.tile.x},${build.tile.y}"
                else
                    "Build:X,X"
            }.touchable(Touchable.disabled).name("mouse-position-build")
            row()
            label {
                val pos = Screen.worldOnMouse()
                "Build:${pos.x.toInt()},${pos.y.toInt()}"
            }.touchable(Touchable.disabled).name("mouse-position-world").uniformX()
            row()
        }
        val debug = WidgetGroup()
        Core.scene.add(debug)
        debug.addChildAt(0, Table().apply {
            visible {
                Debug.enableUnlockContent
            }
            button("Lock") {
                NewBaseDialog.apply {
                    cont.table(Tex.button) { t ->
                        t.button("Lock All") {
                            ForEachUnlockableContent {
                                it.lock()
                            }
                        }.growX().row()
                        t.button("Select One To Lock") {
                            DatabaseSelectorDialog.apply {
                                onClick = {
                                    NewBaseDialog.apply {
                                        cont.add("Confirm lock ${it.localizedName} ?").row()
                                        cont.button("Lock") {
                                            it.lock()
                                            hide()
                                        }.width(150f)
                                        addCloseButton()
                                    }.show()
                                }
                            }.show()
                        }.growX().row()
                    }.width(300f)
                    addCloseButton()
                }.show()
            }.width(150f)
            button("Unlock") {
                NewBaseDialog.apply {
                    cont.table(Tex.button) { t ->
                        t.button("Unlock All") {
                            ForEachUnlockableContent {
                                it.unlock()
                            }
                        }.growX().row()
                        t.button("Select One To Unlock") {
                            DatabaseSelectorDialog.apply {
                                onClick = {
                                    NewBaseDialog.apply {
                                        cont.add("Confirm unlock ${it.localizedName} ?").row()
                                        cont.button("Unlock") {
                                            it.unlock()
                                            hide()
                                        }.width(150f)
                                        addCloseButton()
                                    }.show()
                                }
                            }.show()
                        }.growX().row()
                    }.width(300f)
                    addCloseButton()
                }.show()
            }.width(150f)
            bottom()
            left()
        })
    }
}