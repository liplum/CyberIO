package net.liplum.ui

import arc.scene.ui.Label
import arc.scene.ui.layout.Table
import mindustry.ctype.UnlockableContent
import mindustry.ui.dialogs.BaseDialog
import net.liplum.Var
import net.liplum.registry.CioBlock
import net.liplum.registry.CioHoloUnit

object IconGenDebugDialog {
    val debugged by lazy {
        listOf<UnlockableContent>(
            CioBlock.holoWall,
            CioBlock.holoWallLarge,
            CioHoloUnit.holoMiner,
            CioHoloUnit.holoFighter,
            CioHoloUnit.holoGuardian,
            CioHoloUnit.holoSupporter,
            CioHoloUnit.holoArchitect,
        )
    }

    fun show() {
        BaseDialog("Debug Icon Generating").apply {
            val icons = Table()
            fun rebuild() {
                icons.clear()
                debugged.forEachIndexed { i, it ->
                    icons.addTable {
                        image(it.uiIcon).size(100f).row()
                        add(it.localizedName)
                    }.size(120f).pad(10f)
                    if (i != 0 && i % 5 == 0) {
                        icons.row()
                    }
                }
            }
            rebuild()
            cont.add(icons).grow().row()
            val holoWallAlpha = Label("HoloWall: ${(Var.HoloWallTintAlpha * 255).toInt()} ->${Var.HoloWallTintAlpha}")
            val holoUnitAlpha = Label("HoloUnit: ${(Var.HoloUnitTintAlpha * 255).toInt()} ->${Var.HoloUnitTintAlpha}")
            cont.add(holoWallAlpha).row()
            cont.add(holoUnitAlpha).row()
            fun reload() {
                debugged.forEach {
                    it.loadIcon()
                }
                rebuild()
            }
            cont.slider(0f, 1f, 0.0001f, Var.HoloWallTintAlpha) {
                Var.HoloWallTintAlpha = it
                holoWallAlpha.setText("HoloWall: ${(it * 255).toInt()} -> $it")
                reload()
            }.width(1000f).row()

            cont.slider(0f, 1f, 0.0001f, Var.HoloUnitTintAlpha) {
                Var.HoloUnitTintAlpha = it
                holoUnitAlpha.setText("HoloUnit: ${(it * 255).toInt()} -> $it")
                reload()
            }.width(1000f).row()
            addCloseButton()

            buttons.button("Reload") {
                reload()
            }
        }.show()
    }
}