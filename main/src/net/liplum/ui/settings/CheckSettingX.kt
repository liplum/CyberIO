package net.liplum.ui.settings

import arc.Core
import arc.scene.Element
import arc.scene.ui.CheckBox
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable.Setting
import net.liplum.ui.addTrackTooltip

class CheckSettingX(
    name: String,
    val def: Boolean,
    val onChanged: (Boolean) -> Unit,
) : Setting(name), ISettingCondition {
    var canShow: () -> Boolean = { true }
    override fun add(table: SettingsTable) {
        val box = CheckBox(title)
        box.update {
            box.isChecked = Core.settings.getBool(name)
        }
        box.changed {
            Core.settings.put(name, box.isChecked)
            onChanged(box.isChecked)
        }
        box.left()
        val prefTable = table.table().left().padTop(3f).get()
        prefTable.add(box)
        table.row()
    }

    override fun addDesc(elem: Element) {
        if (description == null) return
        elem.addTrackTooltip(description)
    }

    override fun canShow(): Boolean =
        this.canShow.invoke()

    companion object {
        fun SettingsTable.addCheckPref(
            name: String, def: Boolean,
            onChanged: (Boolean) -> Unit = {},
        ): CheckSettingX =
            CheckSettingX(name, def, onChanged).apply {
                settings.add(this)
                Core.settings.defaults(name, def)
                rebuild()
            }
    }
}