package net.liplum.ui

import arc.Core.settings
import mindustry.Vars
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable.CheckSetting
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable.SliderSetting
import net.liplum.R
import net.liplum.Settings

object SettingsUI {
    @JvmStatic
    fun addGraphicSettings() {
        val graphics = Vars.ui.settings.graphics
        graphics.insertSliderPref(
            R.Setting.LinkOpacity,
            100, 0, 100, 5, InsertPos.After, { "$it%" }, {
                Settings.LinkOpacity = settings.getInt(R.Setting.LinkOpacity) / 100f
            }) {
            it !is SliderSetting
        }
        val alwaysShowLinkDefault = Vars.mobile
        graphics.insertCheckPref(
            R.Setting.AlwaysShowLink,
            alwaysShowLinkDefault, InsertPos.Before, {
                Settings.AlwaysShowLink = settings.getBool(R.Setting.AlwaysShowLink)
            }
        ) {
            it is CheckSetting
        }
    }
}