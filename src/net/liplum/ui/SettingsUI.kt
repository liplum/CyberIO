package net.liplum.ui

import arc.Core.settings
import arc.math.Interp
import mindustry.Vars
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable.CheckSetting
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable.SliderSetting
import net.liplum.R
import net.liplum.Settings
import net.liplum.lib.ui.settings.InsertPos
import net.liplum.lib.ui.settings.insertCheckPref
import net.liplum.lib.ui.settings.insertCheckPrefLast
import net.liplum.lib.ui.settings.insertSliderPref
import net.liplum.utils.invoke

object SettingsUI {
    @JvmStatic
    fun appendSettings() {
        addGraphicSettings()
        addGameSettings()
    }
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
        // input [0,100] -> output [0,30]
        val pct2Density: (Int) -> Float = {
            Interp.pow2Out(it / 100f) * 30f
        }
        graphics.insertSliderPref(
            R.Setting.LinkArrowDensity,
            15, 0, 100, 5, InsertPos.After, { "$it%" }, {
                Settings.LinkArrowDensity = pct2Density(settings.getInt(R.Setting.LinkArrowDensity, 15))
            }) {
            it !is SliderSetting
        }
        val alwaysShowLinkDefault = Vars.mobile
        graphics.insertCheckPref(
            R.Setting.AlwaysShowLink,
            alwaysShowLinkDefault, InsertPos.Before, {
                Settings.AlwaysShowLink = settings.getBool(R.Setting.AlwaysShowLink, alwaysShowLinkDefault)
            }
        ) {
            it is CheckSetting
        }
        graphics.insertSliderPref(
            R.Setting.LinkSize,
            100, 0, 100, 5, InsertPos.After, { "$it%" }, {
                Settings.LinkSize = settings.getInt(R.Setting.LinkSize, 100) / 100f * 4f
            }) {
            it !is SliderSetting
        }
        graphics.insertCheckPref(
            R.Setting.ShowLinkCircle,
            alwaysShowLinkDefault, InsertPos.Before, {
                Settings.ShowLinkCircle = settings.getBool(R.Setting.ShowLinkCircle, true)
            }
        ) {
            it is CheckSetting
        }
    }
    @JvmStatic
    fun addGameSettings() {
        val game = Vars.ui.settings.game
        game.insertCheckPrefLast(
            R.Setting.ShowUpdate, !Vars.steam
        )
        game.insertCheckPrefLast(
            R.Setting.ShowWelcome, true
        )
    }
}