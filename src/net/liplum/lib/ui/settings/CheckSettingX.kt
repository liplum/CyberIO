package net.liplum.lib.ui.settings

import arc.Core
import arc.scene.Element
import arc.scene.ui.CheckBox
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable.Setting
import net.liplum.lib.ui.addTrackTooltip

class CheckSettingX(
    name: String,
    val def: Boolean,
    val onChanged: (Boolean) -> Unit,
) : Setting(name) {
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
        addDesc(table.add(box).left().padTop(3f).get())
        table.row()
    }
    override fun addDesc(elem: Element) {
        if (description == null) return
        elem.addTrackTooltip(description)
    }
}