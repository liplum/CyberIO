package net.liplum.lib.ui.settings

import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable.Setting

class AnySetting(
    val ctor: AnySetting.(SettingsTable) -> Unit
) : Setting(null) {
    override fun add(table: SettingsTable) {
        this.ctor(table)
        table.row()
    }
}