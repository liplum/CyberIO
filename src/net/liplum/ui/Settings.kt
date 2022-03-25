package net.liplum.ui

import arc.Core.settings
import arc.scene.event.ChangeListener
import mindustry.Vars
import net.liplum.R

object Settings {
    @JvmField var LinkOpacity = 0f
    @JvmStatic
    fun addGraphicSettings() {
        val graphics = Vars.ui.settings.graphics
        graphics.sliderPref(R.Setting.LinkOpacity, 0, 0, 100, 5) {
            "$it%"
        }
        graphics.addListener {
            if (it is ChangeListener.ChangeEvent) {
                LinkOpacity = settings.getInt(R.Setting.LinkOpacity) / 100f
            }
            false
        }
    }
}