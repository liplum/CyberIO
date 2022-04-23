package net.liplum.ui

import arc.scene.event.Touchable
import arc.scene.ui.layout.Table
import mindustry.Vars
import net.liplum.R
import net.liplum.inputs.Screen
import net.liplum.utils.bundle

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
    }
}