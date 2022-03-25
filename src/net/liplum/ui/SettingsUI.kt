package net.liplum.ui

import arc.Core.settings
import arc.scene.event.ChangeListener
import mindustry.Vars
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable.SliderSetting
import net.liplum.R
import net.liplum.Settings.LinkOpacity

object SettingsUI {
    @JvmStatic
    fun addGraphicSettings() {
        val graphics = Vars.ui.settings.graphics
        graphics.insertSliderPrefAfter(
            R.Setting.LinkOpacity, 100, 0, 100, 5, { "$it%" }) {
            it !is SliderSetting
        }
        graphics.addListener {
            if (it is ChangeListener.ChangeEvent) {
                LinkOpacity = settings.getInt(R.Setting.LinkOpacity) / 100f
            }
            false
        }
    }
}