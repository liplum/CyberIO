package net.liplum.ui

import arc.Core
import arc.scene.event.Touchable
import arc.scene.ui.layout.Table
import arc.scene.ui.layout.WidgetGroup
import mindustry.Vars
import net.liplum.R
import net.liplum.inputs.Screen
import net.liplum.utils.ForEachUnlockableContent
import net.liplum.utils.bundle
import net.liplum.utils.lock

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
            }.touchable(Touchable.disabled).name("mouse-position-world")
            row()
        }
        val debug = WidgetGroup()
        Core.scene.add(debug)
        debug.addChildAt(0, Table().apply {
            button("Lock") {
                ForEachUnlockableContent {
                    it.lock()
                }
            }.width(150f)
            button("Unlock") {
                ForEachUnlockableContent {
                    it.unlock()
                }
            }.width(150f)
            bottom()
            left()
        })
    }
}