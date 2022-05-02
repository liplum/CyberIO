package net.liplum.ui

import arc.Core
import arc.Events
import arc.math.Interp
import arc.scene.ui.TextButton
import arc.scene.ui.layout.Table
import mindustry.Vars
import mindustry.game.EventType.Trigger
import mindustry.ui.Styles
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable.CheckSetting
import net.liplum.Meta
import net.liplum.R
import net.liplum.Settings
import net.liplum.UnsteamOnly
import net.liplum.lib.ui.settings.SliderSettingX
import net.liplum.lib.ui.settings.SliderSettingX.Companion.addSliderSettingX
import net.liplum.lib.ui.settings.addCheckPref
import net.liplum.lib.ui.settings.sort
import net.liplum.utils.getF
import net.liplum.utils.invoke

object CioUI {
    @JvmStatic
    fun appendSettings() {
        addCyberIOSettingMenu()
    }
    @JvmStatic
    fun addCyberIOSettingMenu() {
        val uiSettings = Vars.ui.settings
        val menu = uiSettings.getF<Table>("menu")
        val prefs = uiSettings.getF<Table>("prefs")
        uiSettings.resized {
            settings.rebuild()
            uiSettings.updateScrollFocus()
        }
        Events.run(Trigger.update) {
            if (Vars.ui.settings.isShown) {
                val cioSettings = menu.find<TextButton>(SettingButtonName)
                if (cioSettings == null) {
                    menu.row()
                    menu.button(Meta.Name, Styles.cleart) {
                        prefs.clearChildren()
                        prefs.add(settings)
                    }.get().apply {
                        name = SettingButtonName
                    }
                }
            }
        }
    }

    const val SettingButtonName = "cyber-io-settings-button"
    val settings = SettingsTable().apply {
        addSliderSettingX(R.Setting.LinkOpacity,
            100, 0, 100, 5, { "$it%" }
        ) {
            Settings.LinkOpacity = Core.settings.getInt(R.Setting.LinkOpacity) / 100f
        }
        // input [0,100] -> output [0,30]
        val pct2Density: (Int) -> Float = {
            Interp.pow2Out(it / 100f) * 30f
        }
        addSliderSettingX(R.Setting.LinkArrowDensity,
            15, 0, 100, 5, { "$it" }
        ) {
            Settings.LinkArrowDensity = pct2Density(Core.settings.getInt(R.Setting.LinkArrowDensity, 15))
        }
        val alwaysShowLinkDefault = Vars.mobile
        addCheckPref(R.Setting.AlwaysShowLink, alwaysShowLinkDefault) {
            Settings.AlwaysShowLink = Core.settings.getBool(R.Setting.AlwaysShowLink, alwaysShowLinkDefault)
        }
        addSliderSettingX(
            R.Setting.LinkSize,
            100, 0, 100, 5, { "$it%" }) {
            Settings.LinkSize = Core.settings.getInt(R.Setting.LinkSize, 100) / 100f * 4f
        }
        addCheckPref(R.Setting.ShowLinkCircle, alwaysShowLinkDefault) {
            Settings.ShowLinkCircle = Core.settings.getBool(R.Setting.ShowLinkCircle, true)
        }
        UnsteamOnly {
            addCheckPref(
                R.Setting.ShowUpdate, !Vars.steam
            )
        }
        addCheckPref(
            R.Setting.ShowWelcome, true
        )
        sort(
            mapOf(
                SliderSettingX::class.java to 0,
                CheckSetting::class.java to 1,
            )
        )
    }
}