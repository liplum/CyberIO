package net.liplum.ui

import arc.Core
import arc.func.Boolc
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable.CheckSetting
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable.Setting
import mindustry.ui.dialogs.SettingsMenuDialog.StringProcessor
import net.liplum.utils.insertAfter
import net.liplum.utils.insertBefore

enum class InsertPos {
    After, Before
}

fun SettingsTable.insertSliderPref(
    name: String, def: Int, min: Int, max: Int, step: Int,
    insertPos: InsertPos = InsertPos.After,
    s: StringProcessor = StringProcessor {it.toString()},
    onChanged: () -> Unit = {},
    whenTrue: (Setting) -> Boolean
): SliderSettingX {
    val res = SliderSettingX(name, def, min, max, step, onChanged, s)
    if (insertPos == InsertPos.After) {
        settings.insertAfter(res, whenTrue)
    } else {
        settings.insertBefore(res, whenTrue)
    }
    Core.settings.defaults(name, def)
    rebuild()
    return res
}

fun SettingsTable.insertCheckPref(
    name: String, def: Boolean,
    insertPos: InsertPos = InsertPos.After,
    onChanged: Boolc = Boolc {},
    whenTrue: (Setting) -> Boolean
): CheckSetting {
    val res = CheckSetting(name, def, onChanged)
    if (insertPos == InsertPos.After) {
        settings.insertBefore(res, whenTrue)
    } else {
        settings.insertAfter(res, whenTrue)
    }
    Core.settings.defaults(name, def)
    rebuild()
    return res
}

