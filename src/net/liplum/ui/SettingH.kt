package net.liplum.ui

import arc.Core
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable.Setting
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable.SliderSetting
import mindustry.ui.dialogs.SettingsMenuDialog.StringProcessor
import net.liplum.utils.insertAfter
import net.liplum.utils.insertBefore

fun SettingsTable.insertSliderPrefAfter(
    name: String, def: Int, min: Int, max: Int, step: Int,
    s: StringProcessor, whenTrue: (Setting) -> Boolean
): SliderSetting {
    val res = SliderSetting(name, def, min, max, step, s)
    settings.insertAfter(res, whenTrue)
    Core.settings.defaults(name, def)
    rebuild()
    return res
}

fun SettingsTable.insertSliderPrefBefore(
    name: String, def: Int, min: Int, max: Int, step: Int,
    s: StringProcessor, whenTrue: (Setting) -> Boolean
): SliderSetting {
    val res = SliderSetting(name, def, min, max, step, s)
    settings.insertBefore(res, whenTrue)
    Core.settings.defaults(name, def)
    rebuild()
    return res
}