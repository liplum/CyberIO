package net.liplum.ui

import arc.scene.ui.Label
import arc.scene.ui.layout.Table
import mindustry.ctype.UnlockableContent
import mindustry.ui.dialogs.BaseDialog
import net.liplum.holo.HoloWall

object IconGenDebugDialog {
    val debugged = ArrayList<UnlockableContent>()
    fun show() {
        BaseDialog("Debug Icon Generating").apply {
            val icons = Table()
            fun rebuild() {
                icons.clear()
                debugged.forEach {
                    icons.addTable {
                        image(it.uiIcon).size(100f).row()
                        add(it.localizedName)
                    }.size(120f).pad(10f)
                }
            }
            rebuild()
            cont.add(icons).grow().row()
            val alpha255 = Label("${(HoloWall.holoTintAlpha * 255).toInt()}")
            val alpha = Label("${HoloWall.holoTintAlpha}")
            cont.add(alpha255).row()
            cont.add(alpha).row()
            fun reload() {
                debugged.forEach {
                    it.loadIcon()
                }
                rebuild()
            }
            cont.slider(0f, 1f, 0.0001f, HoloWall.holoTintAlpha) {
                HoloWall.holoTintAlpha = it
                alpha.setText("$it")
                alpha255.setText("${(it * 255).toInt()}")
                reload()
            }.width(1000f)
            addCloseButton()

            buttons.button("Reload") {
                reload()
            }
        }.show()
    }
}