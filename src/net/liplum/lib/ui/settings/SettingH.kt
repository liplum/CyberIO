package net.liplum.lib.ui.settings

import arc.Core
import arc.func.Boolc
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable.CheckSetting
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable.Setting
import mindustry.ui.dialogs.SettingsMenuDialog.StringProcessor
import net.liplum.scripts.KeyNotFoundException
import net.liplum.utils.insertAfter
import net.liplum.utils.insertBefore

enum class InsertPos {
    After, Before
}

inline fun SettingsTable.insertSliderPref(
    name: String, def: Int, min: Int, max: Int, step: Int,
    insertPos: InsertPos = InsertPos.After,
    s: StringProcessor = StringProcessor { it.toString() },
    noinline onChanged: () -> Unit = {},
    whenTrue: (Setting) -> Boolean,
): SliderSettingX {
    val res = SliderSettingX(name, def, min, max, step, s, onChanged)
    if (insertPos == InsertPos.After) {
        settings.insertAfter(res, whenTrue)
    } else {
        settings.insertBefore(res, whenTrue)
    }
    Core.settings.defaults(name, def)
    rebuild()
    return res
}

fun SettingsTable.insertSliderPrefLast(
    name: String, def: Int, min: Int, max: Int, step: Int,
    s: StringProcessor = StringProcessor { it.toString() },
    onChanged: () -> Unit = {},
): SliderSettingX {
    val res = SliderSettingX(name, def, min, max, step, s, onChanged)
    settings.add(res)
    Core.settings.defaults(name, def)
    rebuild()
    return res
}

fun SettingsTable.insertSliderPrefFirst(
    name: String, def: Int, min: Int, max: Int, step: Int,
    s: StringProcessor = StringProcessor { it.toString() },
    onChanged: () -> Unit = {},
): SliderSettingX {
    val res = SliderSettingX(name, def, min, max, step, s, onChanged)
    settings.insert(0, res)
    Core.settings.defaults(name, def)
    rebuild()
    return res
}

inline fun SettingsTable.insertCheckPref(
    name: String, def: Boolean,
    insertPos: InsertPos = InsertPos.After,
    onChanged: Boolc = Boolc {},
    whenTrue: (Setting) -> Boolean,
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

fun SettingsTable.insertCheckPrefLast(
    name: String, def: Boolean,
    onChanged: Boolc = Boolc {},
): CheckSetting =
    CheckSetting(name, def, onChanged).apply {
        settings.add(this)
        Core.settings.defaults(name, def)
        rebuild()
    }

fun SettingsTable.insertCheckPrefFirst(
    name: String, def: Boolean,
    onChanged: Boolc = Boolc {},
): CheckSetting {
    val res = CheckSetting(name, def, onChanged)
    settings.insert(0, res)
    Core.settings.defaults(name, def)
    rebuild()
    return res
}

fun SettingsTable.addCheckPref(
    name: String, def: Boolean,
    onChanged: (Boolean) -> Unit = {},
): CheckSettingX =
    CheckSettingX(name, def, onChanged).apply {
        settings.add(this)
        Core.settings.defaults(name, def)
        rebuild()
    }

fun SettingsTable.sort(priority: Map<Class<out Setting>, Int>) {
    settings.sortComparing {
        priority[it.javaClass] ?: throw KeyNotFoundException("${it.javaClass}")
    }
    rebuild()
}

fun SettingsTable.sortByClz(priority: (Class<out Setting>) -> Int) {
    settings.sortComparing {
        priority(it.javaClass)
    }
    rebuild()
}

fun SettingsTable.sortBy(priority: (Setting) -> Int) {
    settings.sortComparing {
        priority(it)
    }
    rebuild()
}

fun SettingsTable.addAny(
    ctor: AnySetting.(SettingsTable) -> Unit,
): AnySetting {
    val res = AnySetting(ctor)
    settings.add(res)
    rebuild()
    return res
}
