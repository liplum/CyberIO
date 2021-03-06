package net.liplum.ui

import arc.Core
import arc.scene.event.Touchable
import arc.scene.ui.layout.Table
import arc.scene.ui.layout.WidgetGroup
import mindustry.Vars
import mindustry.gen.Tex
import net.liplum.R
import net.liplum.inputs.Screen
import net.liplum.lib.bundle
import net.liplum.lib.lock
import net.liplum.lib.ui.NewBaseDialog
import net.liplum.utils.ForEachUnlockableContent

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
                R.UI.MousePositionTile.bundle(Screen.tileXOnMouse(), Screen.tileYOnMouse())
            }.touchable(Touchable.disabled).name("mouse-position-tile")
            row()
            label {
                val build = Screen.tileOnMouse()?.build
                if (build != null)
                    R.UI.MousePositionBuild.bundle(build.tile.x, build.tile.y)
                else
                    R.UI.MousePositionBuild.bundle("X", "X")
            }.touchable(Touchable.disabled).name("mouse-position-build")
            row()
            label {
                val pos = Screen.worldOnMouse()
                R.UI.MousePositionWorld.bundle(pos.x.toInt(), pos.y.toInt())
            }.touchable(Touchable.disabled).name("mouse-position-world").fillX()
            row()
        }
        val debug = WidgetGroup()
        Core.scene.add(debug)
        debug.addChildAt(0, Table().apply {
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